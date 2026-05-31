package com.example.quickbid.quickbid.mediospago;

import com.example.quickbid.quickbid.common.dto.ApiResponse;
import com.example.quickbid.quickbid.mediospago.dto.CrearMedioPagoRequest;
import com.example.quickbid.quickbid.mediospago.dto.MedioPagoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuario/medios-pago")
@RequiredArgsConstructor
public class MedioPagoController {

    private final MedioPagoService medioPagoService;

    // GET /api/usuario/medios-pago
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<MedioPagoResponse>>>> listar(
            @AuthenticationPrincipal String email) {
        List<MedioPagoResponse> medios = medioPagoService.listar(email);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("medios", medios), "Lista de medios de pago"));
    }

    // POST /api/usuario/medios-pago — JSON (tarjeta / cuenta bancaria)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> crearJson(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody CrearMedioPagoRequest req) {
        Long id = medioPagoService.crear(email, req, null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        Map.of("id", id, "estado", "pendiente_verificacion"),
                        "Medio registrado. Pendiente de verificacion"));
    }

    // POST /api/usuario/medios-pago — multipart (cheque con fotos)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> crearMultipart(
            @AuthenticationPrincipal String email,
            @RequestParam("tipo")             String tipo,
            @RequestParam("moneda")           String moneda,
            @RequestParam("numeroCheque")     String numeroCheque,
            @RequestParam("monto")            java.math.BigDecimal monto,
            @RequestParam("fechaVencimiento") java.time.LocalDate fechaVencimiento,
            @RequestPart("fotoAnverso")       MultipartFile fotoAnverso,
            @RequestPart("fotoReverso")       MultipartFile fotoReverso) {
        CrearMedioPagoRequest req = new CrearMedioPagoRequest();
        req.setTipo(tipo);
        req.setMoneda(moneda);
        req.setNumeroCheque(numeroCheque);
        req.setMonto(monto);
        req.setFechaVencimiento(fechaVencimiento);
        Long id = medioPagoService.crear(email, req, fotoAnverso, fotoReverso);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        Map.of("id", id, "estado", "pendiente_verificacion"),
                        "Medio registrado. Pendiente de verificacion"));
    }

    // DELETE /api/usuario/medios-pago/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        medioPagoService.eliminar(email, id);
        return ResponseEntity.ok(ApiResponse.ok("Medio de pago eliminado correctamente"));
    }

    // PATCH /api/usuario/medios-pago/{id}/principal
    @PatchMapping("/{id}/principal")
    public ResponseEntity<ApiResponse<Void>> marcarPrincipal(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        medioPagoService.marcarPrincipal(email, id);
        return ResponseEntity.ok(ApiResponse.ok("Medio de pago establecido como principal"));
    }
}
