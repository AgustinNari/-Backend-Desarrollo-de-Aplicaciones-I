package com.example.quickbid.quickbid.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.quickbid.quickbid.entity.app.AuditoriaApp;
import com.example.quickbid.quickbid.repository.app.AuditoriaAppRepository;

@Service
public class PersistenceAuditService implements AuditService {

	private final AuditoriaAppRepository auditoriaRepository;

	public PersistenceAuditService(AuditoriaAppRepository auditoriaRepository) {
		this.auditoriaRepository = auditoriaRepository;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void record(AuditEvent event) {
		auditoriaRepository.save(new AuditoriaApp(event));
	}
}
