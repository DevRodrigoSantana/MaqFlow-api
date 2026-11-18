package br.com.rodrigosantana.maqflow.dto.auth;

import br.com.rodrigosantana.maqflow.enums.Role;
import java.util.UUID;

public record UserAuthResponseDTO(UUID id, String name, String email, Role role) {}
