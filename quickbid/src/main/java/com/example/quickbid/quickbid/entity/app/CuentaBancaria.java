package com.example.quickbid.quickbid.entity.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_cuentas_bancarias")
public class CuentaBancaria {

	@Id
	@Column(name = "medio_pago_id")
	private Long medioPagoId;

	@Column(name = "numero_cuenta_hash")
	private String numeroCuentaHash;

	@Column(name = "cbu_cvu_hash")
	private String cbuCvuHash;

	@Column(name = "nombre_banco", nullable = false)
	private String nombreBanco;

	private String alias;

	protected CuentaBancaria() {
	}

	public CuentaBancaria(Long id, String numero, String cbu, String banco, String alias) {
		medioPagoId = id;
		numeroCuentaHash = numero;
		cbuCvuHash = cbu;
		nombreBanco = banco;
		this.alias = alias;
	}

	public String getNombreBanco() {
		return nombreBanco;
	}
}
