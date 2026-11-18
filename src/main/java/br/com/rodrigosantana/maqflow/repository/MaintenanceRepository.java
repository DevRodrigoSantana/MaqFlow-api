package br.com.rodrigosantana.maqflow.repository;

import br.com.rodrigosantana.maqflow.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {
    boolean existsByEquipmentId(UUID id);

    boolean existsByRegisteredById(UUID id);
}
