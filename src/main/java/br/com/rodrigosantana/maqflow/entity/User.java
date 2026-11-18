package br.com.rodrigosantana.maqflow.entity;

import br.com.rodrigosantana.maqflow.enums.Role;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends br.com.rodrigosantana.maqflow.entity.BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private Role role;

    @OneToMany(mappedBy = "createdBy")
    private List<Equipment> equipmentCreated = new ArrayList<>();

    @OneToMany(mappedBy = "registeredBy")
    private List<Maintenance> maintenancesRegistered = new ArrayList<>();

    public User() {}
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<Equipment> getEquipmentCreated() { return equipmentCreated; }
    public List<Maintenance> getMaintenancesRegistered() { return maintenancesRegistered; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordHash() { return password; }
    public boolean passwordMatches(String candidate, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        return password != null && encoder.matches(candidate, password);
    }
    @Override public String toString() { return "User{id=" + id + ", name='" + name + "', email='" + email + "'}"; }
}
