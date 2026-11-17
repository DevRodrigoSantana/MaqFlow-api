package br.com.rodrigosantana.maqflow.dto.equipment;
import br.com.rodrigosantana.maqflow.enums.EquipmentStatus;
import java.util.UUID;
public record EquipmentResponseDTO(UUID id, String name, String description, String serialNumber, EquipmentStatus status, UUID createdById) {}
