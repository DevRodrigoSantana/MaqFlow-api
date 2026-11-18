package br.com.rodrigosantana.maqflow.dto.auth;

public record AuthResponseDTO(String accessToken, String refreshToken, String tokenType,
                              long expiresIn, long refreshExpiresIn, UserAuthResponseDTO user) {}
