package com.example.quickbid.quickbid.notificacion;

import com.example.quickbid.quickbid.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuario/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    // GET /api/usuario/notificaciones?categoria=todo|subastas|transacciones&leidas=false&page=1
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listar(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean leidas,
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificacionService.listar(email, categoria, leidas, page),
                "Lista de notificaciones"));
    }

    // PATCH /api/usuario/notificaciones/{id}/leer  (id puede ser un numero o "all")
    @PatchMapping("/{id}/leer")
    public ResponseEntity<ApiResponse<Void>> marcarLeida(
            @AuthenticationPrincipal String email,
            @PathVariable String id) {
        notificacionService.marcarLeida(email, id);
        return ResponseEntity.ok(ApiResponse.ok("Notificacion marcada como leida"));
    }
}
