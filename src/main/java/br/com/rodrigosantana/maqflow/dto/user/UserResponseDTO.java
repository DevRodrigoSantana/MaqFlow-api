package br.com.rodrigosantana.maqflow.dto.user;

import br.com.rodrigosantana.maqflow.enums.Role;

import java.util.UUID;

public record UserResponseDTO(UUID id, String name, String email, Role role) {
}
