package br.com.rodrigosantana.maqflow.dto.user;
import br.com.rodrigosantana.maqflow.enums.Role;
public record UserRequestDTO(String name, String email, String password, Role role) {}
