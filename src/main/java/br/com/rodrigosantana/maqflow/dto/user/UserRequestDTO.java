package br.com.rodrigosantana.maqflow.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
        String name,
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail deve ter formato válido.")
        @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres.")
        String email,
        @NotBlank(message = "Senha é obrigatória.")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
                message = "Senha deve ter no mínimo 8 caracteres, incluindo maiúscula, minúscula, número e caractere especial.")
        String password) {
}
