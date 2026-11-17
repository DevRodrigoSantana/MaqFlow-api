package br.com.rodrigosantana.maqflow.mapper;

import br.com.rodrigosantana.maqflow.dto.user.*;
import br.com.rodrigosantana.maqflow.entity.User;

public class UserMapper {
    public User toEntity(UserRequestDTO d) {
        User u = new User();
        u.setName(d.name());
        u.setEmail(d.email());
        u.setPassword(d.password());
        u.setRole(br.com.rodrigosantana.maqflow.enums.Role.USER);
        return u;
    }

    public void update(User u, UserUpdateRequestDTO d) {
        u.setName(d.name());
        u.setEmail(d.email());
    }

    public void updatePassword(User u, UserPasswordUpdateDTO d) {
        u.setPassword(d.newPassword());
    }

    public UserResponseDTO toResponse(User u) {
        return new UserResponseDTO(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
