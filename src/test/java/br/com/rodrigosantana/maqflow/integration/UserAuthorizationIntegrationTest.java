package br.com.rodrigosantana.maqflow.integration;

import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.enums.Role;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAuthorizationIntegrationTest {
    private static final String USERS = "/api/v1/users";
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;

    @Test
    void userCanManageOwnProfileAndPasswordButCannotManageOtherAccounts() throws Exception {
        String ownEmail = "user-owner-" + UUID.randomUUID() + "@example.test";
        String otherEmail = "user-other-" + UUID.randomUUID() + "@example.test";
        UUID ownId = register(ownEmail);
        UUID otherId = register(otherEmail);
        String access = login(ownEmail, "ValidPass1!");

        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(access)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(ownId.toString()));
        mvc.perform(get(USERS).header("Authorization", bearer(access)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(get(USERS + "/" + otherId).header("Authorization", bearer(access)))
                .andExpect(status().isForbidden());
        mvc.perform(get(USERS + "/" + ownId).header("Authorization", bearer(access)))
                .andExpect(status().isForbidden());
        mvc.perform(delete(USERS + "/" + ownId).header("Authorization", bearer(access)))
                .andExpect(status().isForbidden());
        mvc.perform(delete(USERS + "/" + otherId).header("Authorization", bearer(access)))
                .andExpect(status().isForbidden());

        mvc.perform(put(USERS + "/" + otherId).header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("Should Not Change", otherEmail, "ValidPass1!")))
                .andExpect(status().isForbidden());
        assertEquals("Authorization Test", users.findById(otherId).orElseThrow().getName());

        mvc.perform(put(USERS + "/" + otherId + "/password").header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"ValidPass1!\",\"newPassword\":\"Changed1!\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(put(USERS + "/" + ownId).header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("Own Profile Updated", ownEmail, "ValidPass1!")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Own Profile Updated"));
        mvc.perform(put(USERS + "/" + ownId + "/password").header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"ValidPass1!\",\"newPassword\":\"Changed1!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void adminCanQueryAndManageOtherUserAccounts() throws Exception {
        String adminEmail = "user-admin-" + UUID.randomUUID() + "@example.test";
        String targetEmail = "user-target-" + UUID.randomUUID() + "@example.test";
        UUID adminId = register(adminEmail);
        UUID targetId = register(targetEmail);
        User admin = users.findById(adminId).orElseThrow();
        admin.setRole(Role.ADMIN);
        users.saveAndFlush(admin);
        String access = login(adminEmail, "ValidPass1!");

        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(access)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adminId.toString()))
                .andExpect(jsonPath("$.email").value(adminEmail))
                .andExpect(jsonPath("$.role").value("ADMIN"));
        mvc.perform(get(USERS).header("Authorization", bearer(access)))
                .andExpect(status().isOk());
        mvc.perform(get(USERS + "/" + targetId).header("Authorization", bearer(access)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(targetId.toString()));
        mvc.perform(put(USERS + "/" + targetId).header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("Admin Updated", targetEmail, "ValidPass1!")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Admin Updated"));
        mvc.perform(put(USERS + "/" + targetId + "/password").header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"ValidPass1!\",\"newPassword\":\"Changed1!\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete(USERS + "/" + targetId).header("Authorization", bearer(access)))
                .andExpect(status().isNoContent());
        assertFalse(users.existsById(targetId));
    }

    @Test
    void publicRegistrationCannotCreateAnAdminRole() throws Exception {
        String email = "public-role-" + UUID.randomUUID() + "@example.test";
        MvcResult result = mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Public Signup\",\"email\":\"" + email
                                + "\",\"password\":\"ValidPass1!\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();
        UUID createdId = responseId(result);
        assertEquals(Role.USER, users.findById(createdId).orElseThrow().getRole());
    }

    @Test
    void userCanUseEquipmentAndMaintenanceEndpoints() throws Exception {
        String email = "user-resources-" + UUID.randomUUID() + "@example.test";
        UUID userId = register(email);
        String access = login(email, "ValidPass1!");

        String serial = "AUTH-" + UUID.randomUUID();
        MvcResult equipmentCreated = mvc.perform(post("/api/v1/equipment")
                        .header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Torno de teste\",\"description\":\"Teste de autorização\","
                                + "\"serialNumber\":\"" + serial + "\",\"status\":\"ACTIVE\","
                                + "\"createdById\":\"" + userId + "\"}"))
                .andExpect(status().isCreated()).andReturn();
        UUID equipmentId = responseId(equipmentCreated);
        mvc.perform(get("/api/v1/equipment").header("Authorization", bearer(access)))
                .andExpect(status().isOk()).andExpect(content().string(containsString(serial)));
        mvc.perform(get("/api/v1/equipment/" + equipmentId).header("Authorization", bearer(access)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(equipmentId.toString()));
        mvc.perform(put("/api/v1/equipment/" + equipmentId)
                        .header("Authorization", bearer(access)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Torno atualizado\",\"description\":\"Atualizado\","
                                + "\"serialNumber\":\"" + serial + "\",\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Torno atualizado"));

        MvcResult maintenanceCreated = mvc.perform(post("/api/v1/maintenances")
                        .header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Manutenção de teste\",\"type\":\"PREVENTIVE\","
                                + "\"status\":\"OPEN\",\"equipmentId\":\"" + equipmentId
                                + "\",\"registeredById\":\"" + userId + "\"}"))
                .andExpect(status().isCreated()).andReturn();
        UUID maintenanceId = responseId(maintenanceCreated);
        mvc.perform(get("/api/v1/maintenances").header("Authorization", bearer(access)))
                .andExpect(status().isOk()).andExpect(content().string(containsString(maintenanceId.toString())));
        mvc.perform(get("/api/v1/maintenances/" + maintenanceId).header("Authorization", bearer(access)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(maintenanceId.toString()));
        mvc.perform(put("/api/v1/maintenances/" + maintenanceId)
                        .header("Authorization", bearer(access)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Manutenção atualizada\",\"type\":\"CORRECTIVE\","
                                + "\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Manutenção atualizada"));
        mvc.perform(delete("/api/v1/maintenances/" + maintenanceId).header("Authorization", bearer(access)))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/equipment/" + equipmentId).header("Authorization", bearer(access)))
                .andExpect(status().isNoContent());
    }

    private UUID register(String email) throws Exception {
        MvcResult result = mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Authorization Test\",\"email\":\"" + email + "\",\"password\":\"ValidPass1!\"}"))
                .andExpect(status().isCreated()).andReturn();
        return responseId(result);
    }

    private UUID responseId(MvcResult result) throws Exception {
        Matcher matcher = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"")
                .matcher(result.getResponse().getContentAsString());
        assertTrue(matcher.find());
        return UUID.fromString(matcher.group(1));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        Matcher matcher = Pattern.compile("\\\"accessToken\\\":\\\"([^\\\"]+)\\\"")
                .matcher(result.getResponse().getContentAsString());
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private String profileBody(String name, String email, String currentPassword) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"currentPassword\":\"" + currentPassword + "\"}";
    }

    private String bearer(String token) { return "Bearer " + token; }
}
