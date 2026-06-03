package com.example.quickbid.quickbid.repository.app;
import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository; import com.example.quickbid.quickbid.entity.app.RefreshToken;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long>{
 Optional<RefreshToken> findByTokenHash(String hash);
 List<RefreshToken> findAllByCuentaIdAndRevokedAtIsNull(Long cuentaId);
}
