package com.example.quickbid.quickbid.entity.app;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_movimientos_puntos")
public class MovimientoPuntos {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "cuenta_id", nullable = false)
	private Long cuentaId;

	@Column(nullable = false)
	private Integer delta;

	@Column(nullable = false)
	private String motivo;

	@Column(name = "referencia_tipo")
	private String referenciaTipo;

	@Column(name = "referencia_id")
	private Long referenciaId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected MovimientoPuntos() {
	}

	public MovimientoPuntos(Long cuentaId, Integer delta, String motivo, String tipo, Long referencia) {
		this.cuentaId = cuentaId;
		this.delta = delta;
		this.motivo = motivo;
		this.referenciaTipo = tipo;
		this.referenciaId = referencia;
		this.createdAt = OffsetDateTime.now();
	}
}
