package com.example.quickbid.quickbid.entity.app;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_archivos")
public class ArchivoApp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "owner_cuenta_id")
	private Long ownerCuentaId;

	@Column(name = "tipo_contexto", nullable = false)
	private String tipoContexto;

	@Column(name = "filename_original", nullable = false)
	private String filenameOriginal;

	@Column(name = "content_type", nullable = false)
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private Long sizeBytes;

	@Column(name = "storage_path", nullable = false)
	private String storagePath;

	@Column(nullable = false)
	private String checksum;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected ArchivoApp() {
	}
	public ArchivoApp(String tipo, String filename, String contentType, long size, String path, String checksum) {
		this.tipoContexto=tipo; this.filenameOriginal=filename; this.contentType=contentType; this.sizeBytes=size;
		this.storagePath=path; this.checksum=checksum; this.createdAt=OffsetDateTime.now();
	}
	public ArchivoApp(Long ownerCuentaId, String tipo, String filename, String contentType, long size, String path, String checksum) {
		this(tipo, filename, contentType, size, path, checksum); this.ownerCuentaId=ownerCuentaId;
	}

	public Long getId() {
		return id;
	}

	public String getStoragePath() {
		return storagePath;
	}
}
