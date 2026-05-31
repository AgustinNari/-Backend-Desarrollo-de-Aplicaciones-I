package com.example.quickbid.quickbid.common;

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
 * Solo se activa con el perfil "dev" (o sin perfil activo, que es el default).
 * En producción no se ejecuta.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("[DataSeeder] Ya existen usuarios en la BD. Seed omitido.");
            return;
        }

        log.info("[DataSeeder] Cargando datos de prueba...");

        usuarioRepository.save(Usuario.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@quickbid.com")
                .clave(passwordEncoder.encode("password123"))
                .telefono("1145678901")
                .domicilio("Av. Corrientes 1234, CABA")
                .verificado(true)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nombre("María")
                .apellido("González")
                .email("maria@quickbid.com")
                .clave(passwordEncoder.encode("password123"))
                .telefono("1156789012")
                .domicilio("Av. Santa Fe 5678, CABA")
                .verificado(true)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nombre("Carlos")
                .apellido("Rodríguez")
                .email("carlos@quickbid.com")
                .clave(passwordEncoder.encode("password123"))
                .telefono("1167890123")
                .domicilio("Calle Rivadavia 910, CABA")
                .verificado(true)
                .build());

        log.info("[DataSeeder] 3 usuarios de prueba creados.");
        log.info("[DataSeeder] Credenciales: <email> / password123");
        log.info("[DataSeeder]   - juan@quickbid.com");
        log.info("[DataSeeder]   - maria@quickbid.com");
        log.info("[DataSeeder]   - carlos@quickbid.com");
    }
}
