package br.com.rodrigosantana.maqflow.repository;

import br.com.rodrigosantana.maqflow.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    boolean existsByCreatedById(UUID id);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdNot(String serialNumber, UUID id);
}
