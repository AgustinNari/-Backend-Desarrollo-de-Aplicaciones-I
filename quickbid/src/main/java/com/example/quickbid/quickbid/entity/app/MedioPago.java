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
@Table(name = "app_medios_pago")
public class MedioPago {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "cuenta_id", nullable = false)
	private Long cuentaId;

	@Column(nullable = false)
	private String tipo;

	@Column(nullable = false)
	private String moneda;

	@Column(nullable = false)
	private String estado;

	@Column(nullable = false)
	private Boolean principal;

	@Column(nullable = false)
	private Boolean nacional;

	@Column(name = "alias_visible")
	private String aliasVisible;

	@Column(name = "ultimos_4")
	private String ultimos4;

	@Column(nullable = false)
	private String titular;

	@Column(name = "hash_identificador", nullable = false)
	private String hashIdentificador;

	@Column(name = "limite_monto")
	private BigDecimal limiteMonto;

	@Column(name = "consumo_actual", nullable = false)
	private BigDecimal consumoActual;

	@Column(name = "saldo_garantia")
	private BigDecimal saldoGarantia;

	@Column(name = "verificado_hasta")
	private OffsetDateTime verificadoHasta;

	@Column(name = "verificado_por_empleado_id")
	private Integer verificadoPorEmpleadoId;

	@Column(name = "motivo_rechazo")
	private String motivoRechazo;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "deleted_at")
	private OffsetDateTime deletedAt;

	protected MedioPago() {
	}

	public MedioPago(
			Long cuenta,
			String tipo,
			String moneda,
			boolean nacional,
			String alias,
			String ultimos4,
			String titular,
			String hash,
			BigDecimal saldo) {
		this.cuentaId = cuenta;
		this.tipo = tipo;
		this.moneda = moneda;
		this.estado = "pendiente_verificacion";
		this.principal = false;
		this.nacional = nacional;
		this.aliasVisible = alias;
		this.ultimos4 = ultimos4;
		this.titular = titular;
		this.hashIdentificador = hash;
		this.consumoActual = BigDecimal.ZERO;
		this.saldoGarantia = saldo;
		this.createdAt = OffsetDateTime.now();
		this.updatedAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public Long getCuentaId() {
		return cuentaId;
	}

	public String getTipo() {
		return tipo;
	}

	public String getMoneda() {
		return moneda;
	}

	public String getEstado() {
		return estado;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public Boolean getNacional() {
		return nacional;
	}

	public String getAliasVisible() {
		return aliasVisible;
	}

	public String getUltimos4() {
		return ultimos4;
	}

	public BigDecimal getLimiteMonto() {
		return limiteMonto;
	}

	public BigDecimal getConsumoActual() {
		return consumoActual;
	}

	public BigDecimal getSaldoGarantia() {
		return saldoGarantia;
	}

	public OffsetDateTime getVerificadoHasta() {
		return verificadoHasta;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getDeletedAt() {
		return deletedAt;
	}

	public void markPrincipal(boolean value) {
		principal = value;
		updatedAt = OffsetDateTime.now();
	}

	public void delete() {
		estado = "eliminado";
		principal = false;
		deletedAt = OffsetDateTime.now();
		updatedAt = deletedAt;
	}

	public void verify(Integer employee, BigDecimal limiteAprobado) {
		estado = "verificado";
		limiteMonto = limiteAprobado;
		verificadoPorEmpleadoId = employee;
		verificadoHasta = addBusinessDays(OffsetDateTime.now(), 5);
		motivoRechazo = null;
		updatedAt = OffsetDateTime.now();
	}

	public void expire() {
		estado = "vencido";
		principal = false;
		updatedAt = OffsetDateTime.now();
	}

	public void reject(Integer employee, String reason) {
		estado = "rechazado";
		verificadoPorEmpleadoId = employee;
		motivoRechazo = reason;
		updatedAt = OffsetDateTime.now();
	}

	private OffsetDateTime addBusinessDays(OffsetDateTime start, int days) {
		OffsetDateTime result = start;
		for (int added = 0; added < days;) {
			result = result.plusDays(1);
			var day = result.getDayOfWeek();
			if (day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY) {
				added++;
			}
		}
		return result;
	}
}
