package com.example.quickbid.quickbid.notificacion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    // Listar con filtros opcionales
    Page<Notificacion> findByUsuarioId(Long usuarioId, Pageable pageable);

    Page<Notificacion> findByUsuarioIdAndCategoria(Long usuarioId, String categoria, Pageable pageable);

    Page<Notificacion> findByUsuarioIdAndLeida(Long usuarioId, boolean leida, Pageable pageable);

    Page<Notificacion> findByUsuarioIdAndCategoriaAndLeida(Long usuarioId, String categoria, boolean leida, Pageable pageable);

    // Contar no leidas
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    // Buscar una notificacion del usuario
    Optional<Notificacion> findByIdAndUsuarioId(Long id, Long usuarioId);

    // Marcar todas como leidas
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLectura = CURRENT_TIMESTAMP WHERE n.usuarioId = :usuarioId AND n.leida = false")
    void marcarTodasLeidas(Long usuarioId);
}
