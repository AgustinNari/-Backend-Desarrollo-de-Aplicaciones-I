package com.example.quickbid.quickbid.entity.app;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_pujas_live")
public class PujaLive {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "subasta_id", nullable = false)
	private Integer subastaId;

	@Column(name = "item_catalogo_id", nullable = false)
	private Integer itemCatalogoId;

	@Column(name = "cuenta_id", nullable = false)
	private Long cuentaId;

	@Column(nullable = false, precision = 18, scale = 2)
	private BigDecimal monto;

	@Column(nullable = false, length = 3)
	private String moneda;

	@Column(nullable = false)
	private String estado;

	@Column(nullable = false)
	private Long secuencia;

	@Column(name = "version_estado", nullable = false)
	private Long versionEstado;

	@Column(name = "idempotency_key", nullable = false, unique = true)
	private String idempotencyKey;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected PujaLive() {
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getMonto() {
		return monto;
	}
}
