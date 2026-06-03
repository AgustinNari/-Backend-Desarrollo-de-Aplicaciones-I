package com.example.quickbid.quickbid.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.quickbid.quickbid.audit.AuditEvent;
import com.example.quickbid.quickbid.audit.AuditService;
import com.example.quickbid.quickbid.repository.app.CuentaAppRepository;

@Service
public class AuthFailureService {

	private final CuentaAppRepository cuentas;
	private final AuditService audit;

	public AuthFailureService(CuentaAppRepository cuentas, AuditService audit) {
		this.cuentas = cuentas;
		this.audit = audit;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void failedLogin(Long cuentaId) {
		cuentas.findById(cuentaId).ifPresent(cuenta -> {
			cuenta.loginFailed();
			audit.record(new AuditEvent("usuario", cuentaId, "auth.login_fallido", "cuenta", cuentaId, "{}"));
		});
	}
}
