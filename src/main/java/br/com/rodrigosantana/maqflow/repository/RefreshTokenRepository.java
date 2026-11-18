package br.com.rodrigosantana.maqflow.repository;

import br.com.rodrigosantana.maqflow.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.tokenHash = :hash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("hash") String hash);

    Optional<RefreshToken> findByTokenHash(String hash);
    List<RefreshToken> findAllByFamilyId(UUID familyId);
}
