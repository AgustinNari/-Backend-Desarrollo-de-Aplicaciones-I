package com.example.quickbid.quickbid.repository.app;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quickbid.quickbid.entity.app.MedioPago;

public interface MedioPagoRepository extends JpaRepository<MedioPago, Long> {

	List<MedioPago> findAllByCuentaIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long cuenta);

	Optional<MedioPago> findByIdAndCuentaId(Long id, Long cuenta);

	Optional<MedioPago> findByCuentaIdAndHashIdentificadorAndDeletedAtIsNull(Long cuenta, String hash);

	List<MedioPago> findAllByCuentaIdAndMonedaAndPrincipalTrueAndDeletedAtIsNull(Long cuenta, String moneda);

	List<MedioPago> findAllByEstadoAndVerificadoHastaBeforeAndDeletedAtIsNull(
			String estado,
			OffsetDateTime now);

	@Modifying(flushAutomatically = true)
	@Query(
			value = "UPDATE app_medios_pago SET principal=false,updated_at=CURRENT_TIMESTAMP " +
					"WHERE cuenta_id=:cuentaId AND moneda=:moneda AND id<>:selectedId " +
					"AND principal=true AND deleted_at IS NULL",
			nativeQuery = true)
	int clearOtherActivePrincipals(
			@Param("cuentaId") Long cuentaId,
			@Param("moneda") String moneda,
			@Param("selectedId") Long selectedId);
}
