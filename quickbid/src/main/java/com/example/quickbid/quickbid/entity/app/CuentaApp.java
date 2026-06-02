package com.example.quickbid.quickbid.entity.app;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_cuentas")
public class CuentaApp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "persona_id", nullable = false, unique = true)
	private Integer personaId;

	@Column(name = "cliente_id", nullable = false, unique = true)
	private Integer clienteId;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false, length = 40)
	private String estado;

	@Column(nullable = false)
	private Integer puntos;

	@Column(name = "categoria_calculada", nullable = false, length = 10)
	private String categoriaCalculada;

	@Column(name = "ultimo_acceso_at")
	private OffsetDateTime ultimoAccesoAt;

	@Column(name = "intentos_login", nullable = false)
	private Integer intentosLogin;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected CuentaApp() {
	}

	public CuentaApp(Integer personaId, Integer clienteId, String email, String passwordHash) {
		this.personaId = personaId; this.clienteId = clienteId; this.email = email.toLowerCase();
		this.passwordHash = passwordHash; this.estado = "activa"; this.puntos = 0;
		this.categoriaCalculada = "comun"; this.intentosLogin = 0;
		this.createdAt = OffsetDateTime.now(); this.updatedAt = this.createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getEstado() {
		return estado;
	}

	public Integer getPuntos() {
		return puntos;
	}

	public String getPasswordHash() { return passwordHash; }
	public String getCategoriaCalculada() { return categoriaCalculada; }
	public Integer getPersonaId() { return personaId; }
	public Integer getClienteId() { return clienteId; }
	public void updateCategory(String categoria) { categoriaCalculada = categoria; updatedAt = OffsetDateTime.now(); }
	public void addPoints(int delta) { puntos += delta; if (puntos < 0) puntos = 0; updatedAt = OffsetDateTime.now(); }
	public void loginSucceeded() { ultimoAccesoAt = OffsetDateTime.now(); intentosLogin = 0; updatedAt = ultimoAccesoAt; }
	public void loginFailed() { intentosLogin++; updatedAt = OffsetDateTime.now(); }
	public void changePassword(String hash) { passwordHash=hash; updatedAt=OffsetDateTime.now(); }
	public void changeState(String value) { estado=value; updatedAt=OffsetDateTime.now(); }
}
