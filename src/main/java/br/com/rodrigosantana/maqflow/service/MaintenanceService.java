package br.com.rodrigosantana.maqflow.service;

import br.com.rodrigosantana.maqflow.dto.maintenance.*;
import br.com.rodrigosantana.maqflow.entity.*;
import br.com.rodrigosantana.maqflow.exception.*;
import br.com.rodrigosantana.maqflow.mapper.MaintenanceMapper;
import br.com.rodrigosantana.maqflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class MaintenanceService {
    private final MaintenanceRepository repo;
    private final EquipmentRepository equipment;
    private final UserRepository users;
    private final MaintenanceMapper mapper = new MaintenanceMapper();

    public MaintenanceService(MaintenanceRepository r, EquipmentRepository e, UserRepository u) {
        repo = r;
        equipment = e;
        users = u;
    }

    public MaintenanceResponseDTO create(MaintenanceRequestDTO d) {
        validate(d);
        return mapper.toResponse(repo.save(mapper.toEntity(d, findEquipment(d.equipmentId()), findUser(d.registeredById()))));
    }

    @Transactional(readOnly = true)
    public MaintenanceResponseDTO get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponseDTO> list() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    public MaintenanceResponseDTO update(UUID id, MaintenanceUpdateRequestDTO d) {
        validate(d);
        Maintenance m = find(id);
        mapper.update(m, d);
        return mapper.toResponse(repo.save(m));
    }

    public void delete(UUID id) {
        find(id);
        repo.deleteById(id);
    }

    private Maintenance find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Manutenção não encontrada: " + id));
    }

    private Equipment findEquipment(UUID id) {
        if (id == null)
            throw new InvalidRequestException("Equipamento é obrigatório.", Map.of("equipmentId", "Informe o UUID do equipamento."));
        return equipment.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipamento não encontrado: " + id, Map.of("equipmentId", "Nenhum equipamento corresponde ao UUID informado.")));
    }

    private User findUser(UUID id) {
        if (id == null)
            throw new InvalidRequestException("Usuário responsável é obrigatório.", Map.of("registeredById", "Informe o UUID do usuário responsável."));
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não encontrado: " + id, Map.of("registeredById", "Nenhum usuário corresponde ao UUID informado.")));
    }

    private void validate(MaintenanceRequestDTO d) {
        if (d == null)
            throw new InvalidRequestException("O corpo da requisição é obrigatório.", Map.of("body", "Informe os dados da manutenção."));
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(d.description())) errors.put("description", "Descrição é obrigatória.");
        if (d.type() == null) errors.put("type", "Tipo é obrigatório.");
        if (d.status() == null) errors.put("status", "Status é obrigatório.");
        if (d.equipmentId() == null) errors.put("equipmentId", "Equipamento é obrigatório.");
        if (d.registeredById() == null) errors.put("registeredById", "Usuário responsável é obrigatório.");
        if (!errors.isEmpty()) throw new InvalidRequestException("Há campos obrigatórios inválidos.", errors);
    }

    private void validate(MaintenanceUpdateRequestDTO d) {
        if (d == null)
            throw new InvalidRequestException("O corpo da requisição é obrigatório.", Map.of("body", "Informe os dados da manutenção."));
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(d.description())) errors.put("description", "Descrição é obrigatória.");
        if (d.type() == null) errors.put("type", "Tipo é obrigatório.");
        if (d.status() == null) errors.put("status", "Status é obrigatório.");
        if (!errors.isEmpty()) throw new InvalidRequestException("Há campos obrigatórios inválidos.", errors);
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
