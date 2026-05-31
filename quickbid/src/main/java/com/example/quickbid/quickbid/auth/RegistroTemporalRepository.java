package com.example.quickbid.quickbid.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistroTemporalRepository extends JpaRepository<RegistroTemporal, Long> {
    Optional<RegistroTemporal> findByEmail(String email);
    Optional<RegistroTemporal> findByTokenVerificacion(String token);
    boolean existsByEmail(String email);
}
