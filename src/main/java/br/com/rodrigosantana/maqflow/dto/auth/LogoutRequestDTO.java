package br.com.rodrigosantana.maqflow.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDTO(@NotBlank String refreshToken) {}
