package com.example.quickbid.quickbid.repository.app;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository; import com.example.quickbid.quickbid.entity.app.DireccionEnvio;
public interface DireccionEnvioRepository extends JpaRepository<DireccionEnvio,Long>{
 Optional<DireccionEnvio> findFirstByCuentaIdAndPrincipalTrueAndDeletedAtIsNull(Long cuentaId);
 Optional<DireccionEnvio> findByIdAndCuentaIdAndDeletedAtIsNull(Long id,Long cuentaId);
 List<DireccionEnvio> findAllByCuentaIdAndDeletedAtIsNullOrderByPrincipalDescCreatedAtDesc(Long cuentaId);
 long countByCuentaIdAndDeletedAtIsNull(Long cuentaId);
}
