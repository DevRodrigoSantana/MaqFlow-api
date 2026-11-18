package br.com.rodrigosantana.maqflow.mapper;

import br.com.rodrigosantana.maqflow.dto.maintenance.*;
import br.com.rodrigosantana.maqflow.entity.*;

public class MaintenanceMapper {
    public Maintenance toEntity(MaintenanceRequestDTO d, Equipment e, User u) {
        Maintenance m = new Maintenance();
        m.setDescription(d.description());
        m.setType(d.type());
        m.setStatus(d.status());
        m.setEquipment(e);
        m.setRegisteredBy(u);
        return m;
    }

    public void update(Maintenance m, MaintenanceUpdateRequestDTO d) {
        m.setDescription(d.description());
        m.setType(d.type());
        m.setStatus(d.status());
    }

    public MaintenanceResponseDTO toResponse(Maintenance m) {
        return new MaintenanceResponseDTO(m.getId(), m.getDescription(), m.getType(), m.getStatus(), m.getEquipment().getId(), m.getRegisteredBy().getId());
    }
}
