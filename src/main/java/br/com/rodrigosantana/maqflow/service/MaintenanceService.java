package br.com.rodrigosantana.maqflow.service;
import br.com.rodrigosantana.maqflow.dto.maintenance.*;import br.com.rodrigosantana.maqflow.entity.*;import br.com.rodrigosantana.maqflow.exception.*;import br.com.rodrigosantana.maqflow.mapper.MaintenanceMapper;import br.com.rodrigosantana.maqflow.repository.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@Service @Transactional
public class MaintenanceService {
 private final MaintenanceRepository repo;private final EquipmentRepository equipment;private final UserRepository users;private final MaintenanceMapper mapper=new MaintenanceMapper();
 public MaintenanceService(MaintenanceRepository r,EquipmentRepository e,UserRepository u){repo=r;equipment=e;users=u;}
 public MaintenanceResponseDTO create(MaintenanceRequestDTO d){validate(d);return mapper.toResponse(repo.save(mapper.toEntity(d,findEquipment(d.equipmentId()),findUser(d.registeredById()))));}
 @Transactional(readOnly=true) public MaintenanceResponseDTO get(UUID id){return mapper.toResponse(find(id));}
 @Transactional(readOnly=true) public List<MaintenanceResponseDTO> list(){return repo.findAll().stream().map(mapper::toResponse).toList();}
 public MaintenanceResponseDTO update(UUID id,MaintenanceUpdateRequestDTO d){validate(d);Maintenance m=find(id);mapper.update(m,d);return mapper.toResponse(repo.save(m));}
 public void delete(UUID id){find(id);repo.deleteById(id);}
 private Maintenance find(UUID id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Manutenção não encontrada: "+id));}
 private Equipment findEquipment(UUID id){if(id==null)throw new InvalidRequestException("equipmentId é obrigatório.");return equipment.findById(id).orElseThrow(()->new ResourceNotFoundException("Equipamento não encontrado: "+id));}
 private User findUser(UUID id){if(id==null)throw new InvalidRequestException("registeredById é obrigatório.");return users.findById(id).orElseThrow(()->new ResourceNotFoundException("Usuário responsável não encontrado: "+id));}
 private void validate(MaintenanceRequestDTO d){if(d==null||blank(d.description())||d.type()==null||d.status()==null||d.equipmentId()==null||d.registeredById()==null)throw new InvalidRequestException("Descrição, tipo, status, equipamento e responsável são obrigatórios.");}
 private void validate(MaintenanceUpdateRequestDTO d){if(d==null||blank(d.description())||d.type()==null||d.status()==null)throw new InvalidRequestException("Descrição, tipo e status são obrigatórios.");}
 private boolean blank(String s){return s==null||s.isBlank();}
}
