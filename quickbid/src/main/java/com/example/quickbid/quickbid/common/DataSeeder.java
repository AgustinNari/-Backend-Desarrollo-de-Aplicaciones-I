package com.example.quickbid.quickbid.common;

import com.example.quickbid.quickbid.mediospago.MedioPago;
import com.example.quickbid.quickbid.mediospago.MedioPagoRepository;
import com.example.quickbid.quickbid.notificacion.Notificacion;
import com.example.quickbid.quickbid.notificacion.NotificacionRepository;
import com.example.quickbid.quickbid.usuario.Usuario;
import com.example.quickbid.quickbid.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga datos de prueba al iniciar la aplicación.
 * Idempotente: usa findByEmail para no duplicar usuarios.
 * Solo activo fuera de prod.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository      usuarioRepository;
    private final NotificacionRepository notificacionRepository;
    private final MedioPagoRepository    medioPagoRepository;
    private final PasswordEncoder        passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsuarios();
    }

    // ─── Usuarios ─────────────────────────────────────────────────────────────

    private void seedUsuarios() {
        // Usuarios básicos
        findOrCreate("Juan",     "Perez",     "juan@quickbid.com",    "comun",   false);
        findOrCreate("Maria",    "Gonzalez",  "maria@quickbid.com",   "comun",   false);
        findOrCreate("Carlos",   "Rodriguez", "carlos@quickbid.com",  "comun",   false);

        // Usuario completo para probar todas las pantallas
        Usuario demo = findOrCreate("Nicolas", "Demo", "demo@quickbid.com", "oro", false);

        seedNotificacionesPara(demo);
        seedMediosPagoPara(demo);

        log.info("[DataSeeder] Usuarios listos:");
        log.info("[DataSeeder]   juan@quickbid.com / maria@quickbid.com / carlos@quickbid.com  → password123");
        log.info("[DataSeeder]   demo@quickbid.com → password123  (datos completos para e2e)");
    }

    private Usuario findOrCreate(String nombre, String apellido, String email,
                                  String categoria, boolean multaActiva) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario u = usuarioRepository.save(Usuario.builder()
                    .nombre(nombre)
                    .apellido(apellido)
                    .email(email)
                    .clave(passwordEncoder.encode("password123"))
                    .categoria(categoria)
                    .estadoCuenta("activo")
                    .verificado(true)
                    .build());
            log.info("[DataSeeder] Usuario creado: {}", email);
            return u;
        });
    }

    // ─── Notificaciones ───────────────────────────────────────────────────────

    private void seedNotificacionesPara(Usuario u) {
        if (notificacionRepository.countByUsuarioIdAndLeidaFalse(u.getId()) > 0) return;
        if (notificacionRepository.findByUsuarioId(u.getId(),
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements() > 0) return;

        // Una de cada tipo — cubre todos los estados visuales del front
        notif(u, "puja_ganada",               "subastas",      "Ganaste la subasta del Rolex Submariner",            false);
        notif(u, "puja_superada",             "subastas",      "Ana Lopez supero tu puja por $2.500.000",            false);
        notif(u, "subasta_por_comenzar",      "subastas",      "La subasta de Arte Moderno comienza en 30 minutos",  false);
        notif(u, "catalogo_nuevo",            "subastas",      "Nuevo catalogo disponible: Relojes de Lujo",         true);
        notif(u, "medio_pago_verificado",     "transacciones", "Tu tarjeta Visa **** 4321 fue verificada",           true);
        notif(u, "consignacion_aprobada",     "transacciones", "Tu consignacion del Cuadro fue aprobada",            true);
        notif(u, "consignacion_rechazada",    "transacciones", "Tu consignacion del Reloj no paso la revision",      false);
        notif(u, "documentacion_solicitada",  "transacciones", "Se requiere documentacion adicional para tu lote",   false);
        notif(u, "multa_asignada",            "transacciones", "Se genero una multa de $5.000 por retiro de puja",   false);

        log.info("[DataSeeder] Notificaciones creadas para {}", u.getEmail());
    }

    private void notif(Usuario u, String tipo, String categoria, String mensaje, boolean leida) {
        notificacionRepository.save(Notificacion.builder()
                .usuarioId(u.getId())
                .tipo(tipo)
                .categoria(categoria)
                .mensaje(mensaje)
                .leida(leida)
                .build());
    }

    // ─── Medios de pago ───────────────────────────────────────────────────────

    private void seedMediosPagoPara(Usuario u) {
        long existentes = medioPagoRepository
                .findByUsuarioIdAndEliminadoFalse(u.getId()).size();
        if (existentes > 0) return;

        // Tarjeta crédito Visa (principal)
        medioPagoRepository.save(MedioPago.builder()
                .usuarioId(u.getId())
                .tipo("tarjeta_credito")
                .moneda("USD")
                .titular("Nicolas Demo")
                .datosEnmascarados("**** **** **** 4321")
                .marca("Visa")
                .vencimientoTarjeta("12/27")
                .nacional(false)
                .estado("verificado")
                .esPrincipal(true)
                .build());

        // Tarjeta débito Mastercard
        medioPagoRepository.save(MedioPago.builder()
                .usuarioId(u.getId())
                .tipo("tarjeta_debito")
                .moneda("ARS")
                .titular("Nicolas Demo")
                .datosEnmascarados("**** **** **** 8890")
                .marca("Mastercard")
                .vencimientoTarjeta("08/26")
                .nacional(true)
                .estado("verificado")
                .esPrincipal(false)
                .build());

        // Cuenta bancaria
        medioPagoRepository.save(MedioPago.builder()
                .usuarioId(u.getId())
                .tipo("cuenta_bancaria")
                .moneda("ARS")
                .numeroCuenta("00123456789")
                .nombreBanco("Banco Galicia")
                .aliasCbu("nicolas.demo.galicia")
                .nacional(true)
                .datosEnmascarados("****6789")
                .estado("verificado")
                .esPrincipal(false)
                .build());

        // Tarjeta pendiente de verificación
        medioPagoRepository.save(MedioPago.builder()
                .usuarioId(u.getId())
                .tipo("tarjeta_credito")
                .moneda("USD")
                .titular("Nicolas Demo")
                .datosEnmascarados("**** **** **** 0011")
                .marca("Amex")
                .vencimientoTarjeta("03/28")
                .nacional(false)
                .estado("pendiente_verificacion")
                .esPrincipal(false)
                .build());

        log.info("[DataSeeder] Medios de pago creados para {}", u.getEmail());
    }
}
