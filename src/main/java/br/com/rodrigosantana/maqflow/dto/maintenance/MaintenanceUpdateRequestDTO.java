package br.com.rodrigosantana.maqflow.dto.maintenance;

import br.com.rodrigosantana.maqflow.enums.MaintenanceStatus;
import br.com.rodrigosantana.maqflow.enums.MaintenanceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaintenanceUpdateRequestDTO(
        @NotBlank(message = "Descrição é obrigatória.")
        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres.")
        String description,
        @NotNull(message = "Tipo é obrigatório.")
        MaintenanceType type,
        @NotNull(message = "Status é obrigatório.")
        MaintenanceStatus status) {
}
