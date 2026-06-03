package com.example.quickbid.quickbid.entity.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "personas")
public class Persona {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer identificador;

	@Column(nullable = false, length = 20)
	private String documento;

	@Column(nullable = false, length = 150)
	private String nombre;
	@Column(length = 250) private String direccion;

	@Column(length = 15)
	private String estado;

	protected Persona() {
	}
	public Persona(String documento,String nombre,String direccion){this.documento=documento;this.nombre=nombre;this.direccion=direccion;this.estado="activo";}

	public Integer getIdentificador() {
		return identificador;
	}

	public String getDocumento() {
		return documento;
	}

	public String getNombre() {
		return nombre;
	}

	public String getEstado() {
		return estado;
	}
}
