package br.com.rodrigosantana.maqflow.integration;

import br.com.rodrigosantana.maqflow.entity.Equipment;
import br.com.rodrigosantana.maqflow.entity.Maintenance;
import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.enums.Role;
import br.com.rodrigosantana.maqflow.repository.EquipmentRepository;
import br.com.rodrigosantana.maqflow.repository.MaintenanceRepository;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CrudApiIntegrationTest {
    private static final String USERS = "/api/v1/users";
    private static final String EQUIPMENT = "/api/v1/equipment";
    private static final String MAINTENANCES = "/api/v1/maintenances";
    private static final UUID MISSING_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired JdbcTemplate jdbc;
    @Autowired EquipmentRepository equipment;
    @Autowired MaintenanceRepository maintenances;

    private String lastBody;

    @Test
    void userCrudAndNotFoundAndDuplicateEmail() throws Exception {
        String id = createResource(USERS, userBody("user-one@example.test"), 201);
        assertFalse(lastBody.contains("password"));
        assertTrue(users.existsByEmail("user-one@example.test"));

        User stored = users.findById(UUID.fromString(id)).orElseThrow();
        stored.setRole(Role.ADMIN);
        users.saveAndFlush(stored);

        mvc.perform(get(USERS)).andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("user-one@example.test"));
        mvc.perform(get(USERS + "/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(put(USERS + "/" + id).contentType(MediaType.APPLICATION_JSON).content(userUpdateBody("user-one-updated@example.test")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("user-one-updated@example.test"));
        assertTrue(users.existsByEmail("user-one-updated@example.test"));
        assertEquals(Role.ADMIN, users.findById(UUID.fromString(id)).orElseThrow().getRole());

        mvc.perform(put(USERS + "/" + id + "/password").contentType(MediaType.APPLICATION_JSON).content("{\"newPassword\":\"new-test-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        users.flush();
        assertEquals("new-test-password", jdbc.queryForObject("select password from users where id = ?", String.class, UUID.fromString(id)));
        mvc.perform(put(USERS + "/" + id + "/password").contentType(MediaType.APPLICATION_JSON).content("{\"newPassword\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(userBody("user-one-updated@example.test"))).andExpect(status().isConflict());
        mvc.perform(get(USERS + "/" + MISSING_ID)).andExpect(status().isNotFound());
        mvc.perform(delete(USERS + "/" + id)).andExpect(status().isNoContent());
        assertFalse(users.existsById(UUID.fromString(id)));
    }

    @Test
    void userDeleteIsBlockedWhenEquipmentDependsOnIt() throws Exception {
        String userId = createResource(USERS, userBody("user-equipment@example.test"), 201);
        createResource(EQUIPMENT, equipmentBody("SER-USER-DEPENDENCY", userId), 201);
        mvc.perform(delete(USERS + "/" + userId)).andExpect(status().isBadRequest());
        assertTrue(users.existsById(UUID.fromString(userId)));
    }

    @Test
    void equipmentCrudAndDuplicateSerialAndMissingUser() throws Exception {
        String userId = createResource(USERS, userBody("equipment-owner@example.test"), 201);
        String id = createResource(EQUIPMENT, equipmentBody("SER-CRUD-001", userId), 201);
        assertTrue(lastBody.contains("\"createdById\":\"" + userId + "\""));
        Equipment persisted = equipment.findById(UUID.fromString(id)).orElseThrow();
        assertEquals(UUID.fromString(userId), persisted.getCreatedBy().getId());
        mvc.perform(get(EQUIPMENT)).andExpect(status().isOk()).andExpect(jsonPath("$[0].serialNumber").value("SER-CRUD-001"));
        mvc.perform(get(EQUIPMENT + "/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(put(EQUIPMENT + "/" + id).contentType(MediaType.APPLICATION_JSON).content(equipmentUpdateBody("SER-CRUD-001", "Torno atualizado")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Torno atualizado"));
        Equipment updated = equipment.findById(UUID.fromString(id)).orElseThrow();
        assertEquals("Torno atualizado", updated.getName());
        assertEquals(UUID.fromString(userId), updated.getCreatedBy().getId());
        mvc.perform(post(EQUIPMENT).contentType(MediaType.APPLICATION_JSON).content(equipmentBody("SER-CRUD-001", userId))).andExpect(status().isConflict());
        String secondId = createResource(EQUIPMENT, equipmentBody("SER-CRUD-002", userId), 201);
        mvc.perform(put(EQUIPMENT + "/" + secondId).contentType(MediaType.APPLICATION_JSON).content(equipmentUpdateBody("SER-CRUD-001", "Outro torno")))
                .andExpect(status().isConflict());
        assertEquals("SER-CRUD-002", equipment.findById(UUID.fromString(secondId)).orElseThrow().getSerialNumber());
        mvc.perform(post(EQUIPMENT).contentType(MediaType.APPLICATION_JSON).content(equipmentBody("SER-BAD-USER", MISSING_ID.toString()))).andExpect(status().isNotFound());
        mvc.perform(get(EQUIPMENT + "/" + MISSING_ID)).andExpect(status().isNotFound());
        mvc.perform(delete(EQUIPMENT + "/" + id)).andExpect(status().isNoContent());
        assertFalse(equipment.existsById(UUID.fromString(id)));
    }

    @Test
    void equipmentDeleteIsBlockedWhenMaintenanceDependsOnIt() throws Exception {
        String userId = createResource(USERS, userBody("equipment-maint-owner@example.test"), 201);
        String equipmentId = createResource(EQUIPMENT, equipmentBody("SER-MAINT-DEPENDENCY", userId), 201);
        createResource(MAINTENANCES, maintenanceBody(equipmentId, userId), 201);
        mvc.perform(delete(EQUIPMENT + "/" + equipmentId)).andExpect(status().isBadRequest());
        assertTrue(equipment.existsById(UUID.fromString(equipmentId)));
    }

    @Test
    void maintenanceCrudAndInvalidReferencesAndNotFound() throws Exception {
        String userId = createResource(USERS, userBody("maintenance-user@example.test"), 201);
        String equipmentId = createResource(EQUIPMENT, equipmentBody("SER-MAINT-CRUD", userId), 201);
        String id = createResource(MAINTENANCES, maintenanceBody(equipmentId, userId), 201);
        assertTrue(lastBody.contains("\"equipmentId\":\"" + equipmentId + "\""));
        assertTrue(lastBody.contains("\"registeredById\":\"" + userId + "\""));
        Maintenance persisted = maintenances.findById(UUID.fromString(id)).orElseThrow();
        assertEquals(UUID.fromString(equipmentId), persisted.getEquipment().getId());
        assertEquals(UUID.fromString(userId), persisted.getRegisteredBy().getId());
        mvc.perform(get(MAINTENANCES)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get(MAINTENANCES + "/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(put(MAINTENANCES + "/" + id).contentType(MediaType.APPLICATION_JSON).content(maintenanceUpdateBody("Inspeção atualizada")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Inspeção atualizada"));
        Maintenance updated = maintenances.findById(UUID.fromString(id)).orElseThrow();
        assertEquals("Inspeção atualizada", updated.getDescription());
        assertEquals(UUID.fromString(equipmentId), updated.getEquipment().getId());
        assertEquals(UUID.fromString(userId), updated.getRegisteredBy().getId());
        mvc.perform(post(MAINTENANCES).contentType(MediaType.APPLICATION_JSON).content(maintenanceBody(MISSING_ID.toString(), userId))).andExpect(status().isNotFound());
        mvc.perform(post(MAINTENANCES).contentType(MediaType.APPLICATION_JSON).content(maintenanceBody(equipmentId, MISSING_ID.toString()))).andExpect(status().isNotFound());
        mvc.perform(get(MAINTENANCES + "/" + MISSING_ID)).andExpect(status().isNotFound());
        mvc.perform(delete(MAINTENANCES + "/" + id)).andExpect(status().isNoContent());
        assertFalse(maintenances.existsById(UUID.fromString(id)));
    }

    private String createResource(String url, String body, int expectedStatus) throws Exception {
        MvcResult result = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus)).andReturn();
        lastBody = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"").matcher(lastBody);
        assertTrue(matcher.find(), "Expected response body to contain an id: " + lastBody);
        return matcher.group(1);
    }

    private String userBody(String email) {
        return "{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"test-password\"}";
    }

    private String userUpdateBody(String email) {
        return "{\"name\":\"Updated User\",\"email\":\"" + email + "\"}";
    }

    private String equipmentBody(String serialNumber, String userId) {
        return "{\"name\":\"Torno CNC\",\"description\":\"Equipamento de teste\",\"serialNumber\":\"" + serialNumber + "\",\"status\":\"ACTIVE\",\"createdById\":\"" + userId + "\"}";
    }

    private String equipmentUpdateBody(String serialNumber, String name) {
        return "{\"name\":\"" + name + "\",\"description\":\"Equipamento atualizado\",\"serialNumber\":\"" + serialNumber + "\",\"status\":\"INACTIVE\"}";
    }

    private String maintenanceBody(String equipmentId, String userId) {
        return "{\"description\":\"Inspeção preventiva\",\"type\":\"PREVENTIVE\",\"status\":\"OPEN\",\"equipmentId\":\"" + equipmentId + "\",\"registeredById\":\"" + userId + "\"}";
    }

    private String maintenanceUpdateBody(String description) {
        return "{\"description\":\"" + description + "\",\"type\":\"CORRECTIVE\",\"status\":\"IN_PROGRESS\"}";
    }
}
