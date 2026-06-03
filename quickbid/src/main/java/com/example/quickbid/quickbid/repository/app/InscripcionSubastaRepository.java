package com.example.quickbid.quickbid.repository.app;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quickbid.quickbid.entity.app.InscripcionSubasta;

public interface InscripcionSubastaRepository extends JpaRepository<InscripcionSubasta, Long> {
	Optional<InscripcionSubasta> findBySubastaIdAndCuentaId(Integer subastaId, Long cuentaId);
}
