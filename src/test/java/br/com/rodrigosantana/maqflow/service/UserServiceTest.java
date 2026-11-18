package br.com.rodrigosantana.maqflow.service;

import br.com.rodrigosantana.maqflow.dto.user.UserRequestDTO;
import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.exception.DuplicateEmailException;
import br.com.rodrigosantana.maqflow.repository.*;
import br.com.rodrigosantana.maqflow.security.SafeBcryptPasswordEncoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Test
    void rejectsDuplicateEmail() {
        UserRepository users = mock(UserRepository.class);
        when(users.existsByEmail("a@b.com")).thenReturn(true);
        UserService service = new UserService(users, mock(EquipmentRepository.class), mock(MaintenanceRepository.class), new SafeBcryptPasswordEncoder());
        assertThrows(DuplicateEmailException.class, () -> service.create(new UserRequestDTO("Alice", "a@b.com", "ValidPass1!")));
        verify(users, never()).save(any(User.class));
    }
}
