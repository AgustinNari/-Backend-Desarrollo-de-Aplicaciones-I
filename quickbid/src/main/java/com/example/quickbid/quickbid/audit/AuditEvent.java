package com.example.quickbid.quickbid.audit;

public record AuditEvent(
		String actorType,
		Long actorId,
		String action,
		String entityType,
		Long entityId,
		String metadataJson) {
}
