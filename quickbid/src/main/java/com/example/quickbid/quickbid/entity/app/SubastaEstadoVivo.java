package com.example.quickbid.quickbid.entity.app;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "app_subasta_estado_vivo")
public class SubastaEstadoVivo {

	@Id
	@Column(name = "subasta_id")
	private Integer subastaId;

	@Column(name = "item_catalogo_activo_id")
	private Integer itemCatalogoActivoId;

	@Version
	@Column(nullable = false)
	private Long version;

	@Column(name = "usuarios_conectados", nullable = false)
	private Integer usuariosConectados;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected SubastaEstadoVivo() {
	}

	public Integer getSubastaId() {
		return subastaId;
	}

	public Long getVersion() {
		return version;
	}
}
