package br.com.rodrigosantana.maqflow.service;

import br.com.rodrigosantana.maqflow.dto.user.UserPasswordUpdateDTO;
import br.com.rodrigosantana.maqflow.dto.user.UserUpdateRequestDTO;
import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.exception.InvalidRequestException;
import br.com.rodrigosantana.maqflow.repository.EquipmentRepository;
import br.com.rodrigosantana.maqflow.repository.MaintenanceRepository;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import br.com.rodrigosantana.maqflow.security.SafeBcryptPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class UserPasswordUpdateServiceTest {
    private User user;
    private UserService service;
    private UUID userId;
    private AtomicInteger saveCalls;
    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        encoder = new SafeBcryptPasswordEncoder();
        user.setPassword(encoder.encode("Original1!"));
        saveCalls = new AtomicInteger();
        UserRepository users = proxy(UserRepository.class, (method, args) -> switch (method) {
            case "findById" -> Optional.of(user);
            case "save" -> { saveCalls.incrementAndGet(); yield args[0]; }
            default -> defaultValue(method);
        });
        EquipmentRepository equipment = proxy(EquipmentRepository.class, (method, args) -> defaultValue(method));
        MaintenanceRepository maintenance = proxy(MaintenanceRepository.class, (method, args) -> defaultValue(method));
        service = new UserService(users, equipment, maintenance, encoder);
    }

    @Test
    void allowsPasswordUpdateWhenCurrentPasswordMatches() {
        service.updatePassword(userId, new UserPasswordUpdateDTO("Original1!", "Replacement1!"));
        assertTrue(user.passwordMatches("Replacement1!", encoder));
        assertEquals(1, saveCalls.get());
    }

    @Test
    void rejectsWrongCurrentPasswordWithoutChangingOrSavingPassword() {
        assertThrows(InvalidRequestException.class, () -> service.updatePassword(userId,
                new UserPasswordUpdateDTO("wrong-password", "Replacement1!")));
        assertTrue(user.passwordMatches("Original1!", encoder));
        assertEquals(0, saveCalls.get());
    }

    @Test
    void rejectsMissingCurrentPasswordWithoutSaving() {
        assertThrows(InvalidRequestException.class, () -> service.updatePassword(userId,
                new UserPasswordUpdateDTO(null, "Replacement1!")));
        assertTrue(user.passwordMatches("Original1!", encoder));
        assertEquals(0, saveCalls.get());
    }

    @Test
    void rejectsBlankNewPasswordWithoutSaving() {
        assertThrows(InvalidRequestException.class, () -> service.updatePassword(userId,
                new UserPasswordUpdateDTO("Original1!", " ")));
        assertTrue(user.passwordMatches("Original1!", encoder));
        assertEquals(0, saveCalls.get());
    }

    @Test
    void allowsProfileUpdateWhenCurrentPasswordMatches() {
        service.update(userId, new UserUpdateRequestDTO("Updated Name", "updated@example.test", "Original1!"));
        assertEquals("Updated Name", user.getName());
        assertEquals(1, saveCalls.get());
    }

    @Test
    void rejectsProfileUpdateWhenCurrentPasswordIsWrongWithoutSaving() {
        assertThrows(InvalidRequestException.class, () -> service.update(userId,
                new UserUpdateRequestDTO("Updated Name", "updated@example.test", "wrong-password")));
        assertNull(user.getName());
        assertTrue(user.passwordMatches("Original1!", encoder));
        assertEquals(0, saveCalls.get());
    }

    @Test
    void rejectsProfileUpdateWhenCurrentPasswordIsMissing() {
        assertThrows(InvalidRequestException.class, () -> service.update(userId,
                new UserUpdateRequestDTO("Updated Name", "updated@example.test", " ")));
        assertNull(user.getName());
        assertEquals(0, saveCalls.get());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, RepositoryAnswer answer) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) ->
                answer.answer(method.getName(), args == null ? new Object[0] : args));
    }

    private Object defaultValue(String method) {
        return switch (method) {
            case "existsByEmail", "existsByEmailAndIdNot", "existsByCreatedById", "existsByEquipmentId", "existsByRegisteredById", "existsBySerialNumber", "existsBySerialNumberAndIdNot" -> false;
            case "findById" -> Optional.empty();
            case "findAll" -> java.util.List.of();
            case "count" -> 0L;
            default -> null;
        };
    }

    @FunctionalInterface
    private interface RepositoryAnswer { Object answer(String method, Object[] args); }
}
