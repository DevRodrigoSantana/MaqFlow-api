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
 public UserResponseDTO updatePassword(UUID id,UserPasswordUpdateDTO d){
     validatePasswordUpdate(d);
     User user=find(id);
     if(!user.passwordMatches(d.currentPassword())){
         throw new InvalidRequestException("A senha atual está incorreta.",Map.of("currentPassword","A senha atual não confere."));
     }
     mapper.updatePassword(user,d);
     return mapper.toResponse(repo.save(user));
 }
 public void delete(UUID id){find(id);if(equipment.existsByCreatedById(id)||maintenance.existsByRegisteredById(id))throw new InvalidRequestException("Usuário possui equipamentos ou manutenções relacionados.");repo.deleteById(id);}
 private User find(UUID id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Usuário não encontrado: "+id,Map.of("id","Nenhum usuário corresponde ao UUID informado.")));}
 private void validate(UserRequestDTO d){if(d==null)throw new InvalidRequestException("O corpo da requisição é obrigatório.",Map.of("body","Informe os dados do usuário."));Map<String,String> errors=new LinkedHashMap<>();if(blank(d.name()))errors.put("name","Nome é obrigatório.");if(blank(d.email()))errors.put("email","Email é obrigatório.");if(blank(d.password()))errors.put("password","Senha é obrigatória.");if(!errors.isEmpty())throw new InvalidRequestException("Há campos obrigatórios inválidos.",errors);}
 private void validateUpdate(UserUpdateRequestDTO d){if(d==null)throw new InvalidRequestException("O corpo da requisição é obrigatório.",Map.of("body","Informe os dados do usuário."));Map<String,String> errors=new LinkedHashMap<>();if(blank(d.name()))errors.put("name","Nome é obrigatório.");if(blank(d.email()))errors.put("email","Email é obrigatório.");if(!errors.isEmpty())throw new InvalidRequestException("Há campos obrigatórios inválidos.",errors);}
 private void validatePasswordUpdate(UserPasswordUpdateDTO d){if(d==null)throw new InvalidRequestException("O corpo da requisição é obrigatório.",Map.of("body","Informe as senhas para alteração."));Map<String,String> errors=new LinkedHashMap<>();if(blank(d.currentPassword()))errors.put("currentPassword","Informe a senha atual.");if(blank(d.newPassword()))errors.put("newPassword","Informe a nova senha.");if(!errors.isEmpty())throw new InvalidRequestException("Senha atual e nova senha são obrigatórias.",errors);}
 private boolean blank(String s){return s==null||s.isBlank();}
}
