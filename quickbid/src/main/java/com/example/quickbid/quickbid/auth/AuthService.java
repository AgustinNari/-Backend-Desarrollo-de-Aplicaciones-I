package com.example.quickbid.quickbid.auth;

import com.example.quickbid.quickbid.auth.dto.*;
import com.example.quickbid.quickbid.common.exception.AppException;
import com.example.quickbid.quickbid.common.security.JwtUtil;
import com.example.quickbid.quickbid.usuario.Usuario;
import com.example.quickbid.quickbid.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RegistroTemporalRepository registroTemporalRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ─── Etapa 1: datos personales ────────────────────────────────────────────

    public void etapa1(Etapa1Request req) {
        String email = req.getEmail();

        if (usuarioRepository.existsByEmail(email)) {
            throw new AppException("El email ya está registrado", HttpStatus.CONFLICT);
        }

        RegistroTemporal registro = registroTemporalRepository.findByEmail(email)
                .orElse(RegistroTemporal.builder().email(email).build());

        registro.setNombre(req.getNombre());
        registro.setApellido(req.getApellido());
        registro.setTelefono(req.getTelefono());
        registro.setDomicilio(req.getDomicilio());
        registro.setEtapa(RegistroTemporal.EtapaRegistro.ETAPA_1);

        registroTemporalRepository.save(registro);
    }

    // ─── Etapa 2: foto del DNI ────────────────────────────────────────────────

    public void etapa2(Etapa2Request req) {
        RegistroTemporal registro = getRegistroPendiente(req.getEmail());

        registro.setFotoDni(req.getFotoDni());

        // Generar token de verificación (en producción se enviaría por email)
        String token = UUID.randomUUID().toString();
        registro.setTokenVerificacion(token);
        registro.setTokenExpiracion(LocalDateTime.now().plusHours(24));
        registro.setEtapa(RegistroTemporal.EtapaRegistro.ETAPA_2);

        registroTemporalRepository.save(registro);

        // TODO: enviar email con el token cuando se configure spring-boot-starter-mail
        // DEV: token impreso en consola para pruebas (remover en producción)
        log.info("[DEV] Token de verificación para {}: {}", registro.getEmail(), token);
    }

    // ─── Verificar token de email ─────────────────────────────────────────────

    public void verificarToken(VerificarTokenRequest req) {
        RegistroTemporal registro = registroTemporalRepository.findByTokenVerificacion(req.getToken())
                .orElseThrow(() -> new AppException("Token inválido", HttpStatus.BAD_REQUEST));

        if (registro.getTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new AppException("El token ha expirado", HttpStatus.BAD_REQUEST);
        }

        registro.setEtapa(RegistroTemporal.EtapaRegistro.VERIFICADO);
        registroTemporalRepository.save(registro);
    }

    // ─── Etapa 3: crear clave y finalizar registro ────────────────────────────

    @Transactional
    public void etapa3(Etapa3Request req) {
        RegistroTemporal registro = getRegistroPendiente(req.getEmail());

        if (registro.getEtapa() != RegistroTemporal.EtapaRegistro.VERIFICADO) {
            throw new AppException("Debe verificar el email antes de continuar", HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = Usuario.builder()
                .nombre(registro.getNombre())
                .apellido(registro.getApellido())
                .email(registro.getEmail())
                .telefono(registro.getTelefono())
                .domicilio(registro.getDomicilio())
                .fotoDni(registro.getFotoDni())
                .clave(passwordEncoder.encode(req.getClave()))
                .verificado(true)
                .build();

        usuarioRepository.save(usuario);
        registroTemporalRepository.delete(registro);
    }

    // ─── Reenviar link de verificación ───────────────────────────────────────

    public void reenviarLink(ReenviarLinkRequest req) {
        RegistroTemporal registro = getRegistroPendiente(req.getEmail());

        String token = UUID.randomUUID().toString();
        registro.setTokenVerificacion(token);
        registro.setTokenExpiracion(LocalDateTime.now().plusHours(24));
        registroTemporalRepository.save(registro);

        // TODO: enviar email
        log.info("[DEV] Token de verificación para {}: {}", registro.getEmail(), token);
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException("Credenciales inválidas", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(req.getClave(), usuario.getClave())) {
            throw new AppException("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generarToken(usuario.getEmail());
        return new LoginResponse(token, usuario.getEmail(), usuario.getNombre(), usuario.getApellido());
    }

    // ─── Recuperar clave ──────────────────────────────────────────────────────
    // Decisión de diseño: siempre devuelve OK aunque el email no exista.
    // Esto evita revelar si una dirección está registrada o no (enumeration attack).

    public void recuperarClave(RecuperarClaveRequest req) {
        usuarioRepository.findByEmail(req.getEmail()).ifPresent(usuario -> {
            RegistroTemporal temp = registroTemporalRepository.findByEmail(usuario.getEmail())
                    .orElse(RegistroTemporal.builder().email(usuario.getEmail()).build());

            String token = UUID.randomUUID().toString();
            temp.setTokenVerificacion(token);
            temp.setTokenExpiracion(LocalDateTime.now().plusHours(1));
            registroTemporalRepository.save(temp);

            // TODO: enviar email con el link de recuperación
            log.info("[DEV] Token de recuperación para {}: {}", usuario.getEmail(), token);
        });
    }

    // ─── Cambiar clave ────────────────────────────────────────────────────────

    @Transactional
    public void cambiarClave(CambiarClaveRequest req) {
        RegistroTemporal temp = registroTemporalRepository.findByTokenVerificacion(req.getToken())
                .orElseThrow(() -> new AppException("Token inválido", HttpStatus.BAD_REQUEST));

        if (temp.getTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new AppException("El token ha expirado", HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = usuarioRepository.findByEmail(temp.getEmail())
                .orElseThrow(() -> new AppException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        usuario.setClave(passwordEncoder.encode(req.getNuevaClave()));
        usuarioRepository.save(usuario);
        registroTemporalRepository.delete(temp);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private RegistroTemporal getRegistroPendiente(String email) {
        return registroTemporalRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("No se encontró un registro pendiente para ese email", HttpStatus.NOT_FOUND));
    }
}
