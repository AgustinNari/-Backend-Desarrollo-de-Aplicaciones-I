package com.example.quickbid.quickbid.auth;

import com.example.quickbid.quickbid.auth.dto.*;
import com.example.quickbid.quickbid.common.exception.AppException;
import com.example.quickbid.quickbid.common.security.JwtUtil;
import com.example.quickbid.quickbid.usuario.Usuario;
import com.example.quickbid.quickbid.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RegistroTemporalRepository registroTemporalRepository;
    private final DocumentoIdentidadRepository documentoIdentidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 0 = sin expiracion (util para testing). Cualquier otro valor = minutos de vigencia.
    @Value("${auth.setup-token.expiracion-minutos:15}")
    private int setupTokenExpiracionMinutos;

    // ─── Etapa 1: datos personales ────────────────────────────────────────────

    public Etapa1Response etapa1(Etapa1Request req) {
        String email = req.getEmail();

        if (usuarioRepository.existsByEmail(email)) {
            throw new AppException("El email ya está registrado", HttpStatus.CONFLICT);
        }

        RegistroTemporal registro = registroTemporalRepository.findByEmail(email)
                .orElse(RegistroTemporal.builder().email(email).build());

        registro.setNombre(req.getNombre());
        registro.setApellido(req.getApellido());
        registro.setDomicilioLegal(req.getDomicilioLegal());
        registro.setIdPaisOrigen(req.getIdPaisOrigen());
        registro.setEtapa(RegistroTemporal.EtapaRegistro.ETAPA_1);

        RegistroTemporal saved = registroTemporalRepository.save(registro);
        return new Etapa1Response(saved.getId(), "etapa2_fotos_dni");
    }

    // ─── Etapa 2: fotos del DNI (frente + dorso) ─────────────────────────────

    public void etapa2(String email, MultipartFile fotoFrenteDni, MultipartFile fotoDorsoDni) {
        RegistroTemporal registro = getRegistroPendiente(email);

        if (registro.getEtapa() != RegistroTemporal.EtapaRegistro.ETAPA_1) {
            throw new AppException("Las fotos ya fueron subidas previamente", HttpStatus.CONFLICT);
        }

        try {
            registro.setFotoFrenteDni(fotoFrenteDni.getBytes());
            registro.setFotoDorsoDni(fotoDorsoDni.getBytes());
        } catch (IOException e) {
            throw new AppException("Error al procesar las imagenes", HttpStatus.BAD_REQUEST);
        }

        // Generar token de verificacion (en produccion se envia por email)
        String token = UUID.randomUUID().toString();
        registro.setTokenVerificacion(token);
        registro.setTokenExpiracion(LocalDateTime.now().plusHours(48));
        registro.setEtapa(RegistroTemporal.EtapaRegistro.ETAPA_2);

        registroTemporalRepository.save(registro);

        // TODO: enviar email con el link de verificacion
        log.info("[DEV] Token de verificacion para {}: {}", email, token);
    }

    // ─── Verificar token del link ─────────────────────────────────────────────

    public VerificarTokenResponse verificarToken(VerificarTokenRequest req) {
        RegistroTemporal registro = registroTemporalRepository.findByTokenVerificacion(req.getToken())
                .orElseThrow(() -> new AppException("Enlace invalido", HttpStatus.BAD_REQUEST));

        if (registro.getTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new AppException("El token expiro", HttpStatus.GONE);
        }

        // Generar setup_token para usar en etapa3
        String setupToken = UUID.randomUUID().toString();
        registro.setSetupToken(setupToken);
        // Si expiracion=0 se deshabilita (util para testing local)
        registro.setSetupTokenExpiracion(setupTokenExpiracionMinutos > 0
                ? LocalDateTime.now().plusMinutes(setupTokenExpiracionMinutos)
                : null);
        registro.setTokenVerificacion(null); // invalidar el link usado
        registro.setEtapa(RegistroTemporal.EtapaRegistro.VERIFICADO);
        registroTemporalRepository.save(registro);

        return new VerificarTokenResponse(setupToken);
    }

    // ─── Etapa 3: crear clave y finalizar registro ────────────────────────────

    @Transactional
    public LoginResponse etapa3(Etapa3Request req) {
        if (!req.getClave().equals(req.getClaveConfirmacion())) {
            throw new AppException("La clave y su confirmacion no coinciden", HttpStatus.BAD_REQUEST);
        }

        RegistroTemporal registro = registroTemporalRepository.findBySetupToken(req.getSetupToken())
                .orElseThrow(() -> new AppException("Token temporal invalido o expirado", HttpStatus.UNAUTHORIZED));

        if (registro.getEtapa() != RegistroTemporal.EtapaRegistro.VERIFICADO) {
            throw new AppException("Debe verificar el email antes de continuar", HttpStatus.BAD_REQUEST);
        }

        // Validar expiracion del setup_token (si esta habilitada)
        if (registro.getSetupTokenExpiracion() != null
                && registro.getSetupTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new AppException("El token temporal expiro. Solicita un nuevo link.", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = Usuario.builder()
                .nombre(registro.getNombre())
                .apellido(registro.getApellido())
                .email(registro.getEmail())
                .clave(passwordEncoder.encode(req.getClave()))
                .verificado(true)
                .build();

        Usuario savedUsuario = usuarioRepository.save(usuario);

        // Persistir fotos del DNI antes de eliminar el registro temporal
        if (registro.getFotoFrenteDni() != null && registro.getFotoDorsoDni() != null) {
            documentoIdentidadRepository.save(DocumentoIdentidad.builder()
                    .usuarioId(savedUsuario.getId())
                    .fotoFrente(registro.getFotoFrenteDni())
                    .fotoDorso(registro.getFotoDorsoDni())
                    .fechaCarga(LocalDateTime.now())
                    .build());
        }

        registroTemporalRepository.delete(registro);

        String jwt = jwtUtil.generarToken(usuario.getEmail());
        // Usuario recien registrado: siempre sin medio de pago y sin multas
        return new LoginResponse(jwt, usuario.getEmail(), usuario.getNombre(),
                usuario.getCategoria(), "activo_sin_medio_pago", true, false);
    }

    // ─── Reenviar link de verificacion ───────────────────────────────────────

    public void reenviarLink(ReenviarLinkRequest req) {
        RegistroTemporal registro = getRegistroPendiente(req.getEmail());

        String token = UUID.randomUUID().toString();
        registro.setTokenVerificacion(token);
        registro.setTokenExpiracion(LocalDateTime.now().plusHours(48));
        registroTemporalRepository.save(registro);

        // TODO: enviar email
        log.info("[DEV] Token reenviado para {}: {}", registro.getEmail(), token);
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException("Email no registrado", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(req.getClave(), usuario.getClave())) {
            throw new AppException("Email o clave incorrectos", HttpStatus.UNAUTHORIZED);
        }

        if (!usuario.isVerificado()) {
            throw new AppException("Cuenta bloqueada o pendiente de aprobacion", HttpStatus.FORBIDDEN);
        }

        // TODO: consultar multas y medios de pago reales cuando esten implementados
        boolean requiereMedioPago = false;
        boolean tieneMultasActivas = false;

        String jwt = jwtUtil.generarToken(usuario.getEmail());
        return new LoginResponse(jwt, usuario.getEmail(), usuario.getNombre(),
                usuario.getCategoria(), usuario.getEstadoCuenta(),
                requiereMedioPago, tieneMultasActivas);
    }

    // ─── Recuperar clave ──────────────────────────────────────────────────────
    // Siempre devuelve OK aunque el email no exista (anti-enumeration)

    public void recuperarClave(RecuperarClaveRequest req) {
        usuarioRepository.findByEmail(req.getEmail()).ifPresent(usuario -> {
            RegistroTemporal temp = registroTemporalRepository.findByEmail(usuario.getEmail())
                    .orElse(RegistroTemporal.builder().email(usuario.getEmail()).build());

            String token = UUID.randomUUID().toString();
            temp.setTokenVerificacion(token);
            temp.setTokenExpiracion(LocalDateTime.now().plusHours(1));
            registroTemporalRepository.save(temp);

            // TODO: enviar email con el link de recuperacion
            log.info("[DEV] Token de recuperacion para {}: {}", usuario.getEmail(), token);
        });
    }

    // ─── Cambiar clave ────────────────────────────────────────────────────────

    @Transactional
    public void cambiarClave(CambiarClaveRequest req) {
        RegistroTemporal temp = registroTemporalRepository.findByTokenVerificacion(req.getToken())
                .orElseThrow(() -> new AppException("Token invalido", HttpStatus.UNAUTHORIZED));

        if (temp.getTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new AppException("El token ha expirado", HttpStatus.UNAUTHORIZED);
        }

        if (!req.getNuevaClave().equals(req.getClaveConfirmacion())) {
            throw new AppException("Las claves no coinciden", HttpStatus.BAD_REQUEST);
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
                .orElseThrow(() -> new AppException(
                        "No se encontro un registro pendiente para ese email", HttpStatus.NOT_FOUND));
    }
}
