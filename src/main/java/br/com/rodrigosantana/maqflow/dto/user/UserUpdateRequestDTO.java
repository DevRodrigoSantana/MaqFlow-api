package br.com.rodrigosantana.maqflow.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
        String name,
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail deve ter formato válido.")
        @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres.")
        String email,
        @NotBlank(message = "Senha atual é obrigatória.")
        String currentPassword) {
}
