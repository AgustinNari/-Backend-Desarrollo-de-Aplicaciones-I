package com.example.quickbid.quickbid.repository.app;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quickbid.quickbid.entity.app.CuentaApp;

public interface CuentaAppRepository extends JpaRepository<CuentaApp, Long> {

	Optional<CuentaApp> findByEmailIgnoreCase(String email);
}
