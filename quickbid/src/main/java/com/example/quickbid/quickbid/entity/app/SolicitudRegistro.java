package com.example.quickbid.quickbid.entity.app;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_solicitudes_registro")
public class SolicitudRegistro {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(nullable = false, length = 150)
	private String nombre;

	@Column(nullable = false, length = 150)
	private String apellido;

	@Column(name = "domicilio_legal", nullable = false)
	private String domicilioLegal;

	@Column(name = "id_pais_origen", nullable = false)
	private Integer idPaisOrigen;

	@Column(name = "foto_frente_dni_archivo_id") private Long fotoFrenteDniArchivoId;
	@Column(name = "foto_dorso_dni_archivo_id") private Long fotoDorsoDniArchivoId;
	@Column(name = "persona_id") private Integer personaId;
	@Column(name = "cliente_id") private Integer clienteId;

	@Column(nullable = false, length = 40)
	private String estado;

	@Column(name = "setup_token_hash")
	private String setupTokenHash;
	@Column(name = "motivo_rechazo") private String motivoRechazo;

	@Column(name = "setup_token_expires_at")
	private OffsetDateTime setupTokenExpiresAt;
	@Column(name = "setup_token_used_at") private OffsetDateTime setupTokenUsedAt;

	@Column(name = "last_activity_at", nullable = false)
	private OffsetDateTime lastActivityAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected SolicitudRegistro() {
	}

	public SolicitudRegistro(String email, String nombre, String apellido, String domicilioLegal, Integer pais) {
		this.email=email.toLowerCase(); this.nombre=nombre; this.apellido=apellido; this.domicilioLegal=domicilioLegal;
		this.idPaisOrigen=pais; this.estado="pendiente_etapa2"; this.createdAt=OffsetDateTime.now();
		this.updatedAt=this.createdAt; this.lastActivityAt=this.createdAt;
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
	public String getNombre(){return nombre;} public String getApellido(){return apellido;}
	public String getDomicilioLegal(){return domicilioLegal;} public Integer getIdPaisOrigen(){return idPaisOrigen;}
	public Integer getPersonaId(){return personaId;} public Integer getClienteId(){return clienteId;}
	public String getSetupTokenHash(){return setupTokenHash;} public OffsetDateTime getSetupTokenExpiresAt(){return setupTokenExpiresAt;}
	public OffsetDateTime getSetupTokenUsedAt(){return setupTokenUsedAt;}
	public String getMotivoRechazo(){return motivoRechazo;}
	public void attachDni(Long frente, Long dorso){fotoFrenteDniArchivoId=frente; fotoDorsoDniArchivoId=dorso; estado="pendiente_revision"; touch();}
	public void approve(Integer persona, Integer cliente, String hash){personaId=persona; clienteId=cliente; setupTokenHash=hash; setupTokenExpiresAt=OffsetDateTime.now().plusHours(48); estado="aprobada_pendiente_finalizacion"; touch();}
	public void complete(){setupTokenUsedAt=OffsetDateTime.now(); estado="completada"; touch();}
	public void reject(String reason){motivoRechazo=reason; estado="rechazado"; touch();}
	private void touch(){lastActivityAt=OffsetDateTime.now(); updatedAt=lastActivityAt;}
}
