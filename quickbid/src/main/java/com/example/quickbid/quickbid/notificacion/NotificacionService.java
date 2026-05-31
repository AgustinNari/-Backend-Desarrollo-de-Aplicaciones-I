package com.example.quickbid.quickbid.notificacion;

import com.example.quickbid.quickbid.common.exception.AppException;
import com.example.quickbid.quickbid.notificacion.dto.NotificacionResponse;
import com.example.quickbid.quickbid.usuario.Usuario;
import com.example.quickbid.quickbid.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    // ─── GET /api/usuario/notificaciones ─────────────────────────────────────

    public Map<String, Object> listar(String email, String categoria, Boolean leidas, int page) {
        Usuario usuario = getUsuario(email);
        Long usuarioId = usuario.getId();

        Pageable pageable = PageRequest.of(page - 1, 20, Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        Page<Notificacion> resultado;

        boolean filtrarCategoria = categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("todo");
        boolean filtrarLeidas    = leidas != null;

        if (filtrarCategoria && filtrarLeidas) {
            resultado = notificacionRepository.findByUsuarioIdAndCategoriaAndLeida(usuarioId, categoria, leidas, pageable);
        } else if (filtrarCategoria) {
            resultado = notificacionRepository.findByUsuarioIdAndCategoria(usuarioId, categoria, pageable);
        } else if (filtrarLeidas) {
            resultado = notificacionRepository.findByUsuarioIdAndLeida(usuarioId, leidas, pageable);
        } else {
            resultado = notificacionRepository.findByUsuarioId(usuarioId, pageable);
        }

        List<NotificacionResponse> items = resultado.getContent()
                .stream()
                .map(NotificacionResponse::new)
                .toList();

        long noLeidas = notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);

        return Map.of(
                "total", resultado.getTotalElements(),
                "noLeidas", noLeidas,
                "page", page,
                "notificaciones", items
        );
    }

    // ─── PATCH /api/usuario/notificaciones/{id}/leer ──────────────────────────

    @Transactional
    public void marcarLeida(String email, String idParam) {
        Usuario usuario = getUsuario(email);

        if ("all".equalsIgnoreCase(idParam)) {
            notificacionRepository.marcarTodasLeidas(usuario.getId());
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            throw new AppException("ID de notificacion invalido", HttpStatus.BAD_REQUEST);
        }

        Notificacion notif = notificacionRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new AppException("Notificacion no encontrada", HttpStatus.NOT_FOUND));

        notif.setLeida(true);
        notif.setFechaLectura(LocalDateTime.now());
        notificacionRepository.save(notif);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Usuario no encontrado", HttpStatus.NOT_FOUND));
    }
}
