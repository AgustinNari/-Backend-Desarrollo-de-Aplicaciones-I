package com.example.quickbid.quickbid.entity.app;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_inscripciones_subasta")
public class InscripcionSubasta {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "subasta_id", nullable = false)
	private Integer subastaId;

	@Column(name = "cuenta_id", nullable = false)
	private Long cuentaId;

	@Column(name = "medio_pago_id", nullable = false)
	private Long medioPagoId;

	@Column(nullable = false)
	private String estado;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected InscripcionSubasta() {
	}

	public InscripcionSubasta(Integer subastaId, Long cuentaId, Long medioPagoId, boolean requiereRevision) {
		this.subastaId = subastaId;
		this.cuentaId = cuentaId;
		this.medioPagoId = medioPagoId;
		this.estado = requiereRevision ? "pendiente_validacion" : "aprobada";
		this.createdAt = OffsetDateTime.now();
		this.updatedAt = createdAt;
	}

	public Long getId() { return id; }
	public Integer getSubastaId() { return subastaId; }
	public Long getCuentaId() { return cuentaId; }
	public Long getMedioPagoId() { return medioPagoId; }
	public String getEstado() { return estado; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
}
