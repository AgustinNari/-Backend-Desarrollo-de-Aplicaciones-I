package com.example.quickbid.quickbid.audit;

public interface AuditService {

	void record(AuditEvent event);
}
