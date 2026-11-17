package br.com.rodrigosantana.maqflow.entity;

import br.com.rodrigosantana.maqflow.enums.MaintenanceStatus;
import br.com.rodrigosantana.maqflow.enums.MaintenanceType;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "maintenance")
public class Maintenance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "description", length = 1000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    private MaintenanceType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private MaintenanceStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_user_id")
    private User registeredBy;

    protected Maintenance() {}
    public UUID getId() { return id; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public User getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(User registeredBy) { this.registeredBy = registeredBy; }
}
