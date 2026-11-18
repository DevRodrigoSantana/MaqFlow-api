package br.com.rodrigosantana.maqflow.service;

import br.com.rodrigosantana.maqflow.dto.user.*;
import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.exception.*;
import br.com.rodrigosantana.maqflow.mapper.UserMapper;
import br.com.rodrigosantana.maqflow.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UserService {
    private final UserRepository repo;
    private final EquipmentRepository equipment;
    private final MaintenanceRepository maintenance;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper = new UserMapper();

    public UserService(UserRepository repo, EquipmentRepository equipment, MaintenanceRepository maintenance,
                       PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.equipment = equipment;
        this.maintenance = maintenance;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO create(UserRequestDTO dto) {
        validate(dto);
        if (repo.existsByEmail(dto.email())) throw new DuplicateEmailException("Email já cadastrado.");
        User user = mapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        return mapper.toResponse(repo.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO get(UUID id) { return mapper.toResponse(find(id)); }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> list() { return repo.findAll().stream().map(mapper::toResponse).toList(); }

    public UserResponseDTO update(UUID id, UserUpdateRequestDTO dto) {
        validateUpdate(dto);
        User user = find(id);
        if (!user.passwordMatches(dto.currentPassword(), passwordEncoder)) {
            throw new InvalidRequestException("A senha atual está incorreta.", Map.of("currentPassword", "A senha atual não confere."));
        }
        if (repo.existsByEmailAndIdNot(dto.email(), id)) throw new DuplicateEmailException("Email já cadastrado.");
        mapper.update(user, dto);
        return mapper.toResponse(repo.save(user));
    }

    public UserResponseDTO updatePassword(UUID id, UserPasswordUpdateDTO dto) {
        validatePasswordUpdate(dto);
        User user = find(id);
        if (!user.passwordMatches(dto.currentPassword(), passwordEncoder)) {
            throw new InvalidRequestException("A senha atual está incorreta.", Map.of("currentPassword", "A senha atual não confere."));
        }
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        return mapper.toResponse(repo.save(user));
    }

    public void delete(UUID id) {
        find(id);
        if (equipment.existsByCreatedById(id) || maintenance.existsByRegisteredById(id)) {
            throw new InvalidRequestException("Usuário possui equipamentos ou manutenções relacionados.");
        }
        repo.deleteById(id);
    }

    private User find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id,
                Map.of("id", "Nenhum usuário corresponde ao UUID informado.")));
    }

    private void validate(UserRequestDTO dto) {
        if (dto == null) throw new InvalidRequestException("O corpo da requisição é obrigatório.", Map.of("body", "Informe os dados do usuário."));
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(dto.name())) errors.put("name", "Nome é obrigatório.");
        if (blank(dto.email())) errors.put("email", "Email é obrigatório.");
        if (blank(dto.password())) errors.put("password", "Senha é obrigatória.");
        if (!errors.isEmpty()) throw new InvalidRequestException("Há campos obrigatórios inválidos.", errors);
    }

    private void validateUpdate(UserUpdateRequestDTO dto) {
        if (dto == null) throw new InvalidRequestException("O corpo da requisição é obrigatório.", Map.of("body", "Informe os dados do usuário."));
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(dto.name())) errors.put("name", "Nome é obrigatório.");
        if (blank(dto.email())) errors.put("email", "Email é obrigatório.");
        if (blank(dto.currentPassword())) errors.put("currentPassword", "Senha atual é obrigatória.");
        if (!errors.isEmpty()) throw new InvalidRequestException("Nome, e-mail e senha atual são obrigatórios.", errors);
    }

    private void validatePasswordUpdate(UserPasswordUpdateDTO dto) {
        if (dto == null) throw new InvalidRequestException("O corpo da requisição é obrigatório.", Map.of("body", "Informe as senhas para alteração."));
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(dto.currentPassword())) errors.put("currentPassword", "Informe a senha atual.");
        if (blank(dto.newPassword())) errors.put("newPassword", "Informe a nova senha.");
        if (!errors.isEmpty()) throw new InvalidRequestException("Senha atual e nova senha são obrigatórias.", errors);
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
