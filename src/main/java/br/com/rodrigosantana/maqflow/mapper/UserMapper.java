package br.com.rodrigosantana.maqflow.mapper;

import br.com.rodrigosantana.maqflow.dto.user.*;
import br.com.rodrigosantana.maqflow.entity.User;

public class UserMapper {
    public User toEntity(UserRequestDTO d) {
        User u = new User();
        update(u, d);
        return u;
    }

    public void update(User u, UserRequestDTO d) {
        u.setName(d.name());
        u.setEmail(d.email());
        if (d.password() != null && !d.password().isBlank()) u.setPassword(d.password());
        u.setRole(d.role());
    }

    public UserResponseDTO toResponse(User u) {
        return new UserResponseDTO(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
