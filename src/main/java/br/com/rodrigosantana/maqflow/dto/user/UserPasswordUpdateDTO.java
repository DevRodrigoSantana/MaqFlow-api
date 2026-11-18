package br.com.rodrigosantana.maqflow.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserPasswordUpdateDTO(
        @NotBlank(message = "Senha atual é obrigatória.")
        String currentPassword,
        @NotBlank(message = "Nova senha é obrigatória.")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
                message = "Nova senha deve ter no mínimo 8 caracteres, incluindo maiúscula, minúscula, número e caractere especial.")
        String newPassword) {
}
