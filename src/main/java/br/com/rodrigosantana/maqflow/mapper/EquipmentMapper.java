package br.com.rodrigosantana.maqflow.mapper;
import br.com.rodrigosantana.maqflow.dto.equipment.*; import br.com.rodrigosantana.maqflow.entity.Equipment; import br.com.rodrigosantana.maqflow.entity.User;
public class EquipmentMapper {
 public Equipment toEntity(EquipmentRequestDTO d,User u){Equipment e=new Equipment();update(e,d,u);return e;}
 public void update(Equipment e,EquipmentRequestDTO d,User u){e.setName(d.name());e.setDescription(d.description());e.setSerialNumber(d.serialNumber());e.setStatus(d.status());e.setCreatedBy(u);}
 public EquipmentResponseDTO toResponse(Equipment e){return new EquipmentResponseDTO(e.getId(),e.getName(),e.getDescription(),e.getSerialNumber(),e.getStatus(),e.getCreatedBy().getId());}
}
