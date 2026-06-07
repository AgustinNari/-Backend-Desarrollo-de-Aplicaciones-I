package com.example.quickbid.quickbid.entity.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "paises")
public class Pais {

	@Id
	private Integer numero;

	@Column(nullable = false)
	private String nombre;

	@Column(name = "\"nombreCorto\"")
	private String nombreCorto;

	@Column(nullable = false)
	private String capital;

	@Column(nullable = false)
	private String nacionalidad;

	protected Pais() {
	}

	public Integer getNumero() {
		return numero;
	}

	public String getNombre() {
		return nombre;
	}

	public String getNombreCorto() {
		return nombreCorto;
	}

	public String getCapital() {
		return capital;
	}

	public String getNacionalidad() {
		return nacionalidad;
	}
}
