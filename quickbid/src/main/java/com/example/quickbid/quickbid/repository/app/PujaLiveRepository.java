package com.example.quickbid.quickbid.repository.app;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quickbid.quickbid.entity.app.PujaLive;

public interface PujaLiveRepository extends JpaRepository<PujaLive, Long> {

	Optional<PujaLive> findByIdempotencyKey(String idempotencyKey);
}
