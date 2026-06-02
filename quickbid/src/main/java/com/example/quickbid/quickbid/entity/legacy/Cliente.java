package com.example.quickbid.quickbid.entity.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente {

	@Id
	private Integer identificador;
	@Column(name = "\"numeroPais\"") private Integer numeroPais;
	@Column(length = 2) private String admitido;

	@Column(length = 10)
	private String categoria;

	@Column(nullable = false)
	private Integer verificador;

	protected Cliente() {
	}
	public Cliente(Integer id,Integer pais,Integer verificador){this.identificador=id;this.numeroPais=pais;this.verificador=verificador;this.admitido="si";this.categoria="comun";}

	public Integer getIdentificador() {
		return identificador;
	}

	public String getCategoria() {
		return categoria;
	}

	public Integer getVerificador() {
		return verificador;
	}
	public void updateCategory(String value) { categoria = value; }
}
