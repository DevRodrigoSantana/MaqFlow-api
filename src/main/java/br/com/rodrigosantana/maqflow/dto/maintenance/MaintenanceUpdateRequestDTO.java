package br.com.rodrigosantana.maqflow.dto.maintenance;

import br.com.rodrigosantana.maqflow.enums.MaintenanceStatus;
import br.com.rodrigosantana.maqflow.enums.MaintenanceType;

public record MaintenanceUpdateRequestDTO(String description, MaintenanceType type, MaintenanceStatus status) {
}
