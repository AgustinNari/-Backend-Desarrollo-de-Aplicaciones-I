package com.example.quickbid.quickbid.mediospago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedioPagoRepository extends JpaRepository<MedioPago, Long> {

    List<MedioPago> findByUsuarioIdAndEliminadoFalse(Long usuarioId);

    Optional<MedioPago> findByIdentificadorAndUsuarioIdAndEliminadoFalse(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndTipoAndDatosEnmascaradosAndEliminadoFalse(
            Long usuarioId, String tipo, String datosEnmascarados);

    @Modifying
    @Query("UPDATE MedioPago m SET m.esPrincipal = false WHERE m.usuarioId = :usuarioId AND m.esPrincipal = true")
    void desmarcarPrincipal(Long usuarioId);
}
