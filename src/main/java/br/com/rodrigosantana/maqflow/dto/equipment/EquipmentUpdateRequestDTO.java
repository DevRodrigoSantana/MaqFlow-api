package br.com.rodrigosantana.maqflow.dto.equipment;

import br.com.rodrigosantana.maqflow.enums.EquipmentStatus;

public record EquipmentUpdateRequestDTO(String name, String description, String serialNumber, EquipmentStatus status) {
}
