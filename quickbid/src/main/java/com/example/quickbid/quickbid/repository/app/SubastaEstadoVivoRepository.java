package com.example.quickbid.quickbid.repository.app;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quickbid.quickbid.entity.app.SubastaEstadoVivo;

import jakarta.persistence.LockModeType;

public interface SubastaEstadoVivoRepository extends JpaRepository<SubastaEstadoVivo, Integer> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select estado from SubastaEstadoVivo estado where estado.subastaId = :subastaId")
	Optional<SubastaEstadoVivo> findBySubastaIdForUpdate(@Param("subastaId") Integer subastaId);
}
