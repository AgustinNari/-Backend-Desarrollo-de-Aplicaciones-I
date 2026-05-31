package com.example.quickbid.quickbid.perfil;

import com.example.quickbid.quickbid.common.dto.ApiResponse;
import com.example.quickbid.quickbid.perfil.dto.EstadisticasResponse;
import com.example.quickbid.quickbid.perfil.dto.HistorialResponse;
import com.example.quickbid.quickbid.perfil.dto.PerfilResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    // GET /api/usuario/perfil
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<PerfilResponse>> getPerfil(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok(perfilService.getPerfil(email), "Perfil del usuario"));
    }

    // GET /api/usuario/estadisticas?periodo=mes|trimestre|anual
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<EstadisticasResponse>> getEstadisticas(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false, defaultValue = "mes") String periodo) {
        return ResponseEntity.ok(ApiResponse.ok(perfilService.getEstadisticas(email, periodo), "Metricas calculadas"));
    }

    // GET /api/usuario/historial?page=1&limit=20
    @GetMapping("/historial")
    public ResponseEntity<ApiResponse<HistorialResponse>> getHistorial(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(perfilService.getHistorial(email, page, limit), "Historial de pujas"));
    }
}
