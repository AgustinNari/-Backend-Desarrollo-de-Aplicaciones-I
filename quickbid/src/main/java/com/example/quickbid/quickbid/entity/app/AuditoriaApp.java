package com.example.quickbid.quickbid.entity.app;

import java.time.OffsetDateTime;

import com.example.quickbid.quickbid.audit.AuditEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_auditoria")
public class AuditoriaApp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "actor_tipo", nullable = false)
	private String actorTipo;

	@Column(name = "actor_id")
	private Long actorId;

	@Column(nullable = false)
	private String accion;

	@Column(name = "entidad_tipo", nullable = false)
	private String entidadTipo;

	@Column(name = "entidad_id")
	private Long entidadId;

	@Column(name = "metadata_json")
	private String metadataJson;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected AuditoriaApp() {
	}

	public AuditoriaApp(AuditEvent event) {
		this.actorTipo = event.actorType();
		this.actorId = event.actorId();
		this.accion = event.action();
		this.entidadTipo = event.entityType();
		this.entidadId = event.entityId();
		this.metadataJson = event.metadataJson();
		this.createdAt = OffsetDateTime.now();
	}
}
