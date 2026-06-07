package com.example.quickbid.quickbid.repository.app;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Modifying; import org.springframework.data.jpa.repository.Query; import org.springframework.data.repository.query.Param; import com.example.quickbid.quickbid.entity.app.DireccionEnvio;
public interface DireccionEnvioRepository extends JpaRepository<DireccionEnvio,Long>{
 Optional<DireccionEnvio> findFirstByCuentaIdAndPrincipalTrueAndDeletedAtIsNull(Long cuentaId);
 Optional<DireccionEnvio> findByIdAndCuentaIdAndDeletedAtIsNull(Long id,Long cuentaId);
 List<DireccionEnvio> findAllByCuentaIdAndDeletedAtIsNullOrderByPrincipalDescCreatedAtDesc(Long cuentaId);
 long countByCuentaIdAndDeletedAtIsNull(Long cuentaId);
 @Modifying(flushAutomatically=true)
 @Query(value="UPDATE app_direcciones_envio SET principal=false,updated_at=CURRENT_TIMESTAMP WHERE cuenta_id=:cuentaId AND id<>:selectedId AND principal=true AND deleted_at IS NULL",nativeQuery=true)
 int clearOtherActivePrincipals(@Param("cuentaId") Long cuentaId,@Param("selectedId") Long selectedId);
}
