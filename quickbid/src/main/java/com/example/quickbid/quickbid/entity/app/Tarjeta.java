package com.example.quickbid.quickbid.entity.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_tarjetas")
public class Tarjeta {

	@Id
	@Column(name = "medio_pago_id")
	private Long medioPagoId;

	private String marca;

	@Column(name = "vencimiento_mes", nullable = false)
	private Short vencimientoMes;

	@Column(name = "vencimiento_anio", nullable = false)
	private Short vencimientoAnio;

	protected Tarjeta() {
	}

	public Tarjeta(Long id, String marca, int mes, int anio) {
		medioPagoId = id;
		this.marca = marca;
		vencimientoMes = (short) mes;
		vencimientoAnio = (short) anio;
	}

	public String getMarca() {
		return marca;
	}
}
