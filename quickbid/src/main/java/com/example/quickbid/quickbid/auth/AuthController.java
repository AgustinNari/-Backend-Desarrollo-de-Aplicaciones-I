package com.example.quickbid.quickbid.auth;

import com.example.quickbid.quickbid.auth.dto.*;
import com.example.quickbid.quickbid.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/registro/etapa1
    @PostMapping("/registro/etapa1")
    public ResponseEntity<ApiResponse<Void>> etapa1(@Valid @RequestBody Etapa1Request req) {
        authService.etapa1(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Datos personales registrados. Continuá con la foto del DNI."));
    }

    // POST /api/auth/registro/etapa2
    @PostMapping("/registro/etapa2")
    public ResponseEntity<ApiResponse<Void>> etapa2(@Valid @RequestBody Etapa2Request req) {
        authService.etapa2(req);
        return ResponseEntity.ok(ApiResponse.ok("Foto recibida. Te enviamos un link de verificación al email."));
    }

    // POST /api/auth/registro/verificar-token
    @PostMapping("/registro/verificar-token")
    public ResponseEntity<ApiResponse<Void>> verificarToken(@Valid @RequestBody VerificarTokenRequest req) {
        authService.verificarToken(req);
        return ResponseEntity.ok(ApiResponse.ok("Email verificado. Podés crear tu clave."));
    }

    // POST /api/auth/registro/etapa3
    @PostMapping("/registro/etapa3")
    public ResponseEntity<ApiResponse<Void>> etapa3(@Valid @RequestBody Etapa3Request req) {
        authService.etapa3(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registro completado. Ya podés iniciar sesión."));
    }

    // POST /api/auth/registro/reenviar-link
    @PostMapping("/registro/reenviar-link")
    public ResponseEntity<ApiResponse<Void>> reenviarLink(@Valid @RequestBody ReenviarLinkRequest req) {
        authService.reenviarLink(req);
        return ResponseEntity.ok(ApiResponse.ok("Link de verificación reenviado."));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login exitoso."));
    }

    // POST /api/auth/recuperar-clave
    @PostMapping("/recuperar-clave")
    public ResponseEntity<ApiResponse<Void>> recuperarClave(@Valid @RequestBody RecuperarClaveRequest req) {
        authService.recuperarClave(req);
        return ResponseEntity.ok(ApiResponse.ok("Si el email está registrado, recibirás un link para recuperar tu clave."));
    }

    // PUT /api/auth/cambiar-clave
    @PutMapping("/cambiar-clave")
    public ResponseEntity<ApiResponse<Void>> cambiarClave(@Valid @RequestBody CambiarClaveRequest req) {
        authService.cambiarClave(req);
        return ResponseEntity.ok(ApiResponse.ok("Clave actualizada correctamente."));
    }
}
