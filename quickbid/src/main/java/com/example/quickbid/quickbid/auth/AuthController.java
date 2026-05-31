package com.example.quickbid.quickbid.auth;

import com.example.quickbid.quickbid.auth.dto.*;
import com.example.quickbid.quickbid.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/registro/etapa1
    @PostMapping("/registro/etapa1")
    public ResponseEntity<ApiResponse<Etapa1Response>> etapa1(@Valid @RequestBody Etapa1Request req) {
        Etapa1Response data = authService.etapa1(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(data, "Datos registrados correctamente"));
    }

    // POST /api/auth/registro/etapa2  (multipart: email + fotoFrenteDni + fotoDorsoDni)
    @PostMapping(value = "/registro/etapa2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> etapa2(
            @RequestParam("email") String email,
            @RequestParam("fotoFrenteDni") MultipartFile fotoFrenteDni,
            @RequestParam("fotoDorsoDni") MultipartFile fotoDorsoDni) {
        authService.etapa2(email, fotoFrenteDni, fotoDorsoDni);
        return ResponseEntity.ok(ApiResponse.ok("Fotos recibidas. Cuenta en revision"));
    }

    // POST /api/auth/registro/verificar-token
    @PostMapping("/registro/verificar-token")
    public ResponseEntity<ApiResponse<VerificarTokenResponse>> verificarToken(
            @Valid @RequestBody VerificarTokenRequest req) {
        VerificarTokenResponse data = authService.verificarToken(req);
        return ResponseEntity.ok(ApiResponse.ok(data, "Enlace valido. Continua con la creacion de tu clave"));
    }

    // POST /api/auth/registro/etapa3
    @PostMapping("/registro/etapa3")
    public ResponseEntity<ApiResponse<LoginResponse>> etapa3(@Valid @RequestBody Etapa3Request req) {
        LoginResponse data = authService.etapa3(req);
        return ResponseEntity.ok(ApiResponse.ok(data, "Registro finalizado exitosamente"));
    }

    // POST /api/auth/registro/reenviar-link
    @PostMapping("/registro/reenviar-link")
    public ResponseEntity<ApiResponse<Void>> reenviarLink(@Valid @RequestBody ReenviarLinkRequest req) {
        authService.reenviarLink(req);
        return ResponseEntity.ok(ApiResponse.ok("Si el email es valido, se enviara un enlace para continuar"));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse data = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok(data, "Login exitoso"));
    }

    // POST /api/auth/recuperar-clave
    @PostMapping("/recuperar-clave")
    public ResponseEntity<ApiResponse<Void>> recuperarClave(@Valid @RequestBody RecuperarClaveRequest req) {
        authService.recuperarClave(req);
        return ResponseEntity.ok(ApiResponse.ok("Si el email existe se enviara mail"));
    }

    // PUT /api/auth/cambiar-clave
    @PutMapping("/cambiar-clave")
    public ResponseEntity<ApiResponse<Void>> cambiarClave(@Valid @RequestBody CambiarClaveRequest req) {
        authService.cambiarClave(req);
        return ResponseEntity.ok(ApiResponse.ok("Contrasena actualizada exitosamente"));
    }
}
