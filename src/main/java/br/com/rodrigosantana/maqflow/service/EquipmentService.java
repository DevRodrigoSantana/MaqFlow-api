package br.com.rodrigosantana.maqflow.service;
import br.com.rodrigosantana.maqflow.dto.equipment.*;import br.com.rodrigosantana.maqflow.entity.*;import br.com.rodrigosantana.maqflow.exception.*;import br.com.rodrigosantana.maqflow.mapper.EquipmentMapper;import br.com.rodrigosantana.maqflow.repository.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@Service @Transactional
public class EquipmentService {
 private final EquipmentRepository repo;private final UserRepository users;private final MaintenanceRepository maint;private final EquipmentMapper mapper=new EquipmentMapper();
 public EquipmentService(EquipmentRepository r,UserRepository u,MaintenanceRepository m){repo=r;users=u;maint=m;}
 public EquipmentResponseDTO create(EquipmentRequestDTO d){validate(d);ensureSerialNumberAvailable(d.serialNumber());return mapper.toResponse(repo.save(mapper.toEntity(d,findUser(d.createdById()))));}
 @Transactional(readOnly=true) public EquipmentResponseDTO get(UUID id){return mapper.toResponse(find(id));}
 @Transactional(readOnly=true) public List<EquipmentResponseDTO> list(){return repo.findAll().stream().map(mapper::toResponse).toList();}
 public EquipmentResponseDTO update(UUID id,EquipmentRequestDTO d){validate(d);Equipment e=find(id);if(repo.existsBySerialNumberAndIdNot(d.serialNumber(),id))throw duplicateSerialNumber();mapper.update(e,d,findUser(d.createdById()));return mapper.toResponse(repo.save(e));}
 public void delete(UUID id){find(id);if(maint.existsByEquipmentId(id))throw new InvalidRequestException("Equipamento possui manutenções relacionadas.");repo.deleteById(id);}
 private Equipment find(UUID id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Equipamento não encontrado: "+id));}
 private void ensureSerialNumberAvailable(String serialNumber){if(repo.existsBySerialNumber(serialNumber))throw duplicateSerialNumber();}
 private DuplicateSerialNumberException duplicateSerialNumber(){return new DuplicateSerialNumberException("Já existe um equipamento cadastrado com este número de série");}
 private User findUser(UUID id){if(id==null)throw new InvalidRequestException("createdById é obrigatório.");return users.findById(id).orElseThrow(()->new ResourceNotFoundException("Usuário responsável não encontrado: "+id));}
 private void validate(EquipmentRequestDTO d){if(d==null||blank(d.name())||blank(d.serialNumber())||d.status()==null||d.createdById()==null)throw new InvalidRequestException("Nome, número de série, status e responsável são obrigatórios.");}
 private boolean blank(String s){return s==null||s.isBlank();}
}
