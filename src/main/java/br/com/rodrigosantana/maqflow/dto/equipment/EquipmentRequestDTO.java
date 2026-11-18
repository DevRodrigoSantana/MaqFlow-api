package br.com.rodrigosantana.maqflow.dto.equipment;

import br.com.rodrigosantana.maqflow.enums.EquipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record EquipmentRequestDTO(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres.")
        String name,
        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres.")
        String description,
        @NotBlank(message = "Número de série é obrigatório.")
        @Size(min = 1, max = 100, message = "Número de série deve ter entre 1 e 100 caracteres.")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Número de série aceita somente letras, números, hífen e underscore.")
        String serialNumber,
        @NotNull(message = "Status é obrigatório.")
        EquipmentStatus status,
        @NotNull(message = "Usuário responsável é obrigatório.")
        UUID createdById) {
}
