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

	protected Pais() {
	}

	public Integer getNumero() {
		return numero;
	}

	public String getNombre() {
		return nombre;
	}
}
