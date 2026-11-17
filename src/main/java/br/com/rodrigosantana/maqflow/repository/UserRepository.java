package br.com.rodrigosantana.maqflow.repository;
import br.com.rodrigosantana.maqflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID> {
 boolean existsByEmail(String email);
 boolean existsByEmailAndIdNot(String email, UUID id);
}
