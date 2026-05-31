package com.example.quickbid.quickbid.perfil;

import com.example.quickbid.quickbid.common.exception.AppException;
import com.example.quickbid.quickbid.perfil.dto.EstadisticasResponse;
import com.example.quickbid.quickbid.perfil.dto.HistorialResponse;
import com.example.quickbid.quickbid.perfil.dto.PerfilResponse;
import com.example.quickbid.quickbid.usuario.Usuario;
import com.example.quickbid.quickbid.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepository;

    // ─── GET /api/usuario/perfil ──────────────────────────────────────────────

    public PerfilResponse getPerfil(String email) {
        Usuario usuario = getUsuario(email);

        String nombreCompleto = usuario.getNombre() + " " + usuario.getApellido();
        String iniciales = iniciales(usuario.getNombre(), usuario.getApellido());

        return PerfilResponse.builder()
                .email(usuario.getEmail())
                .nombre(nombreCompleto)
                .iniciales(iniciales)
                .categoria(usuario.getCategoria())
                .reputacionPostor(0.0)   // TODO: calcular de tablas del profesor (asistentes/pujos)
                .puntajeAcumulado(0)     // TODO: calcular de tablas del profesor
                .build();
    }

    // ─── GET /api/usuario/estadisticas ────────────────────────────────────────

    public EstadisticasResponse getEstadisticas(String email, String periodo) {
        getUsuario(email); // valida que el usuario exista y el JWT sea valido

        String periodoFinal = (periodo != null && !periodo.isBlank()) ? periodo : "mes";

        // TODO: calcular de tablas del profesor (pujos, registroDeSubasta, asistentes)
        return EstadisticasResponse.builder()
                .periodo(periodoFinal)
                .totalPujado(0L)
                .porcentajeExito(0)
                .totalPagado(0L)
                .serieHistorica(List.of())
                .build();
    }

    // ─── GET /api/usuario/historial ───────────────────────────────────────────

    public HistorialResponse getHistorial(String email, int page, int limit) {
        getUsuario(email);

        int limiteFinal = Math.min(limit, 50);

        // TODO: consultar tablas del profesor (asistentes, pujos, registroDeSubasta, productos)
        return HistorialResponse.builder()
                .total(0)
                .page(page)
                .limit(limiteFinal)
                .items(List.of())
                .build();
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Usuario no encontrado", HttpStatus.NOT_FOUND));
    }

    private String iniciales(String nombre, String apellido) {
        String n = (nombre != null && !nombre.isBlank()) ? String.valueOf(nombre.trim().charAt(0)).toUpperCase() : "";
        String a = (apellido != null && !apellido.isBlank()) ? String.valueOf(apellido.trim().charAt(0)).toUpperCase() : "";
        return n + a;
    }
}
