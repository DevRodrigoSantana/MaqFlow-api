package br.com.rodrigosantana.maqflow.integration;

import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.enums.Role;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JpaAuditingIntegrationTest {
    private static final String USERS = "/api/v1/users";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired JdbcTemplate jdbc;

    @Test
    void publicRegistrationSetsCreationTimestampWithoutInventingAuditor() throws Exception {
        UUID id = register("audit-public-" + UUID.randomUUID() + "@example.test", null);
        User user = users.findById(id).orElseThrow();

        assertNotNull(user.getDataCriacao());
        assertNotNull(user.getDataModificacao());
        assertNull(user.getCriadoPor());
        assertNull(user.getModificadoPor());
    }

    @Test
    void authenticatedCreationAndUpdateCaptureEmailAndPreserveCreationDate() throws Exception {
        String email = "audit-user-" + UUID.randomUUID() + "@example.test";
        UUID id = register(email, null);
        User user = users.findById(id).orElseThrow();
        user.setRole(Role.ADMIN);
        users.saveAndFlush(user);
        String token = login(email);

        UUID equipmentId = createEquipment(id, token);
        var createdAudit = auditValues("equipment", "id", equipmentId);
        LocalDateTime createdAt = createdAudit.createdAt();
        assertNotNull(createdAt);
        assertEquals(email, createdAudit.createdBy());
        assertNotNull(createdAudit.modifiedAt());
        assertEquals(email, createdAudit.modifiedBy());

        mvc.perform(put("/api/v1/equipment/" + equipmentId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Audit Updated\",\"description\":\"updated\","
                                + "\"serialNumber\":\"AUDIT-" + equipmentId + "\",\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());

        var updatedAudit = auditValues("equipment", "id", equipmentId);
        assertEquals(createdAt, updatedAudit.createdAt());
        assertEquals(email, updatedAudit.createdBy());
        assertNotNull(updatedAudit.modifiedAt());
        assertTrue(updatedAudit.modifiedAt().isAfter(createdAudit.modifiedAt())
                        || updatedAudit.modifiedAt().isEqual(createdAudit.modifiedAt()),
                "Modification timestamp must not precede creation timestamp.");
        assertEquals(email, updatedAudit.modifiedBy());
    }

    private UUID register(String email, String token) throws Exception {
        var request = post(USERS).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Audit User\",\"email\":\"" + email
                        + "\",\"password\":\"ValidPass1!\"}");
        if (token != null) request.header("Authorization", bearer(token));
        MvcResult result = mvc.perform(request).andExpect(status().isCreated()).andReturn();
        Matcher matcher = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"")
                .matcher(result.getResponse().getContentAsString());
        assertTrue(matcher.find());
        return UUID.fromString(matcher.group(1));
    }

    private UUID createEquipment(UUID userId, String token) throws Exception {
        String serial = "AUDIT-" + UUID.randomUUID();
        MvcResult result = mvc.perform(post("/api/v1/equipment")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Audit Equipment\",\"description\":\"audit\","
                                + "\"serialNumber\":\"" + serial + "\",\"status\":\"ACTIVE\","
                                + "\"createdById\":\"" + userId + "\"}"))
                .andExpect(status().isCreated()).andReturn();
        Matcher matcher = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"")
                .matcher(result.getResponse().getContentAsString());
        assertTrue(matcher.find());
        return UUID.fromString(matcher.group(1));
    }

    private String login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"ValidPass1!\"}"))
                .andExpect(status().isOk()).andReturn();
        Matcher matcher = Pattern.compile("\\\"accessToken\\\":\\\"([^\\\"]+)\\\"")
                .matcher(result.getResponse().getContentAsString());
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private AuditValues auditValues(String table, String idColumn, UUID id) {
        return jdbc.queryForObject("select data_criacao, data_modificacao, nome_criador, nome_modificador from "
                        + table + " where " + idColumn + " = ?", (rs, row) -> new AuditValues(
                        rs.getTimestamp("data_criacao").toLocalDateTime(),
                        rs.getTimestamp("data_modificacao") == null ? null : rs.getTimestamp("data_modificacao").toLocalDateTime(),
                        rs.getString("nome_criador"), rs.getString("nome_modificador")), id);
    }

    private String bearer(String token) { return "Bearer " + token; }

    private record AuditValues(LocalDateTime createdAt, LocalDateTime modifiedAt,
                               String createdBy, String modifiedBy) { }
}
