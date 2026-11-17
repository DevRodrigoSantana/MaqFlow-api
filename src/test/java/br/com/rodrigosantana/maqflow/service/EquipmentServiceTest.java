package br.com.rodrigosantana.maqflow.service;

import br.com.rodrigosantana.maqflow.dto.equipment.EquipmentRequestDTO;
import br.com.rodrigosantana.maqflow.entity.Equipment;
import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.enums.EquipmentStatus;
import br.com.rodrigosantana.maqflow.exception.DuplicateSerialNumberException;
import br.com.rodrigosantana.maqflow.exception.InvalidRequestException;
import br.com.rodrigosantana.maqflow.repository.EquipmentRepository;
import br.com.rodrigosantana.maqflow.repository.MaintenanceRepository;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipmentServiceTest {
    private final EquipmentRepository equipment = mock(EquipmentRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final MaintenanceRepository maintenances = mock(MaintenanceRepository.class);
    private final EquipmentService service = new EquipmentService(equipment, users, maintenances);

    @Test
    void blocksDeleteWhenMaintenancesExist() {
        UUID id = UUID.randomUUID();
        when(equipment.findById(id)).thenReturn(Optional.of(mock(Equipment.class)));
        when(maintenances.existsByEquipmentId(id)).thenReturn(true);
        assertThrows(InvalidRequestException.class, () -> service.delete(id));
        verify(equipment, never()).deleteById(id);
    }

    @Test
    void rejectsDuplicateSerialNumberOnCreate() {
        UUID userId = UUID.randomUUID();
        EquipmentRequestDTO request = request("SERIAL-001", userId);
        when(equipment.existsBySerialNumber("SERIAL-001")).thenReturn(true);

        DuplicateSerialNumberException exception = assertThrows(
                DuplicateSerialNumberException.class,
                () -> service.create(request));

        assertEquals("Já existe um equipamento cadastrado com este número de série", exception.getMessage());
        verify(equipment, never()).save(any(Equipment.class));
        verify(users, never()).findById(any());
    }

    @Test
    void rejectsSerialNumberUsedByAnotherEquipmentOnUpdate() {
        UUID equipmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(equipment.findById(equipmentId)).thenReturn(Optional.of(mock(Equipment.class)));
        when(equipment.existsBySerialNumberAndIdNot("SERIAL-002", equipmentId)).thenReturn(true);

        DuplicateSerialNumberException exception = assertThrows(
                DuplicateSerialNumberException.class,
                () -> service.update(equipmentId, request("SERIAL-002", userId)));

        assertEquals("Já existe um equipamento cadastrado com este número de série", exception.getMessage());
        verify(equipment, never()).save(any(Equipment.class));
        verify(users, never()).findById(any());
    }

    private EquipmentRequestDTO request(String serialNumber, UUID userId) {
        return new EquipmentRequestDTO("Torno", "Equipamento de teste", serialNumber,
                EquipmentStatus.ACTIVE, userId);
    }
}
