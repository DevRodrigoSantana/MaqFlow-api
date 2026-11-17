package br.com.rodrigosantana.maqflow.entity;

import br.com.rodrigosantana.maqflow.enums.EquipmentStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "equipment", uniqueConstraints = {
        @UniqueConstraint(name = "uk_equipment_serial_number", columnNames = "serial_number")
})
public class Equipment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "name", length = 150)
    private String name;
    @Column(name = "description", length = 1000)
    private String description;
    @Column(name = "serial_number", length = 100)
    private String serialNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private EquipmentStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    @OneToMany(mappedBy = "equipment")
    private List<Maintenance> maintenances = new ArrayList<>();

    public Equipment() {}
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public List<Maintenance> getMaintenances() { return maintenances; }
}
