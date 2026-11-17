package br.com.rodrigosantana.maqflow.dto.maintenance;

import br.com.rodrigosantana.maqflow.enums.MaintenanceStatus;
import br.com.rodrigosantana.maqflow.enums.MaintenanceType;

import java.util.UUID;

public record MaintenanceRequestDTO(String description, MaintenanceType type, MaintenanceStatus status,
                                    UUID equipmentId, UUID registeredById) {
}
