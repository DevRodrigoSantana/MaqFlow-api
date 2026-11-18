package br.com.rodrigosantana.maqflow.service;

import br.com.rodrigosantana.maqflow.dto.user.UserPasswordUpdateDTO;
import br.com.rodrigosantana.maqflow.dto.user.UserUpdateRequestDTO;
import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.exception.InvalidRequestException;
import br.com.rodrigosantana.maqflow.repository.EquipmentRepository;
import br.com.rodrigosantana.maqflow.repository.MaintenanceRepository;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setPassword("original-password");
        saveCalls = new AtomicInteger();
        UserRepository users = proxy(UserRepository.class, (method, args) -> switch (method) {
            case "findById" -> Optional.of(user);
            case "save" -> { saveCalls.incrementAndGet(); yield args[0]; }
            default -> defaultValue(method);
        });
        EquipmentRepository equipment = proxy(EquipmentRepository.class, (method, args) -> defaultValue(method));
        MaintenanceRepository maintenance = proxy(MaintenanceRepository.class, (method, args) -> defaultValue(method));
        service = new UserService(users, equipment, maintenance);
    }

    @Test
    void allowsPasswordUpdateWhenCurrentPasswordMatches() {
        service.updatePassword(userId, new UserPasswordUpdateDTO("original-password", "replacement-password"));
        assertTrue(user.passwordMatches("replacement-password"));
        assertEquals(1, saveCalls.get());
    }

    @Test
    void rejectsWrongCurrentPasswordWithoutChangingOrSavingPassword() {
        InvalidRequestException error = assertThrows(InvalidRequestException.class,
                () -> service.updatePassword(userId, new UserPasswordUpdateDTO("wrong-password", "replacement-password")));
        assertEquals("A senha atual está incorreta.", error.getMessage());
        assertTrue(user.passwordMatches("original-password"));
        assertEquals(0, saveCalls.get());
    }

    @Test
    void rejectsMissingCurrentPasswordWithoutSaving() {
        assertThrows(InvalidRequestException.class,
                () -> service.updatePassword(userId, new UserPasswordUpdateDTO(null, "replacement-password")));
        assertTrue(user.passwordMatches("original-password"));
        assertEquals(0, saveCalls.get());
    }

    @Test
    void rejectsBlankNewPasswordWithoutSaving() {
        assertThrows(InvalidRequestException.class,
                () -> service.updatePassword(userId, new UserPasswordUpdateDTO("original-password", " ")));
        assertTrue(user.passwordMatches("original-password"));
        assertEquals(0, saveCalls.get());
    }

    @Test
    void allowsProfileUpdateWhenCurrentPasswordMatches() {
        service.update(userId, new UserUpdateRequestDTO("Updated Name", "updated@example.test", "original-password"));

        assertEquals("Updated Name", user.getName());
        assertEquals(1, saveCalls.get());
    }

    @Test
    void rejectsProfileUpdateWhenCurrentPasswordIsWrongWithoutSaving() {
        assertThrows(InvalidRequestException.class,
                () -> service.update(userId, new UserUpdateRequestDTO("Updated Name", "updated@example.test", "wrong-password")));

        assertNull(user.getName());
        assertTrue(user.passwordMatches("original-password"));
        assertEquals(0, saveCalls.get());
    }

    @Test
    void rejectsProfileUpdateWhenCurrentPasswordIsMissing() {
        assertThrows(InvalidRequestException.class,
                () -> service.update(userId, new UserUpdateRequestDTO("Updated Name", "updated@example.test", " ")));

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
    private interface RepositoryAnswer {
        Object answer(String method, Object[] args);
    }
}
