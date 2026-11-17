package br.com.rodrigosantana.maqflow.service;
import br.com.rodrigosantana.maqflow.dto.user.*;import br.com.rodrigosantana.maqflow.entity.User;import br.com.rodrigosantana.maqflow.exception.*;import br.com.rodrigosantana.maqflow.mapper.UserMapper;import br.com.rodrigosantana.maqflow.repository.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@Service @Transactional
public class UserService {
 private final UserRepository repo;private final EquipmentRepository equipment;private final MaintenanceRepository maintenance;private final UserMapper mapper=new UserMapper();
 public UserService(UserRepository r,EquipmentRepository e,MaintenanceRepository m){repo=r;equipment=e;maintenance=m;}
 public UserResponseDTO create(UserRequestDTO d){validate(d);if(repo.existsByEmail(d.email()))throw new DuplicateEmailException("Email já cadastrado.");return mapper.toResponse(repo.save(mapper.toEntity(d)));}
 @Transactional(readOnly=true) public UserResponseDTO get(UUID id){return mapper.toResponse(find(id));}
 @Transactional(readOnly=true) public List<UserResponseDTO> list(){return repo.findAll().stream().map(mapper::toResponse).toList();}
 public UserResponseDTO update(UUID id,UserUpdateRequestDTO d){validateUpdate(d);User u=find(id);if(repo.existsByEmailAndIdNot(d.email(),id))throw new DuplicateEmailException("Email já cadastrado.");mapper.update(u,d);return mapper.toResponse(repo.save(u));}
 public UserResponseDTO updatePassword(UUID id,UserPasswordUpdateDTO d){if(d==null||blank(d.newPassword()))throw new InvalidRequestException("A nova senha é obrigatória.");User u=find(id);mapper.updatePassword(u,d);return mapper.toResponse(repo.save(u));}
 public void delete(UUID id){find(id);if(equipment.existsByCreatedById(id)||maintenance.existsByRegisteredById(id))throw new InvalidRequestException("Usuário possui equipamentos ou manutenções relacionados.");repo.deleteById(id);}
 private User find(UUID id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Usuário não encontrado: "+id));}
 private void validate(UserRequestDTO d){if(d==null||blank(d.name())||blank(d.email())||blank(d.password()))throw new InvalidRequestException("Nome, email e senha são obrigatórios.");}
 private void validateUpdate(UserUpdateRequestDTO d){if(d==null||blank(d.name())||blank(d.email()))throw new InvalidRequestException("Nome e email são obrigatórios.");}
 private boolean blank(String s){return s==null||s.isBlank();}
}
