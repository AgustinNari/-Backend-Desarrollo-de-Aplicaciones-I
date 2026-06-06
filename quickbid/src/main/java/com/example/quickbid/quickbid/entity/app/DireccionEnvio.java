package com.example.quickbid.quickbid.entity.app;
import java.time.OffsetDateTime; import jakarta.persistence.*;
@Entity @Table(name="app_direcciones_envio")
public class DireccionEnvio {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="cuenta_id",nullable=false) private Long cuentaId;
 @Column(nullable=false) private String alias; @Column(nullable=false) private String destinatario; @Column(nullable=false) private String calle; @Column(nullable=false) private String numero;
 private String piso; @Column(name="codigo_postal",nullable=false) private String codigoPostal; @Column(nullable=false) private String localidad; @Column(nullable=false) private String provincia; @Column(nullable=false) private String pais; private String telefono;
 @Column(nullable=false) private Boolean principal; @Column(name="created_at",nullable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt; @Column(name="deleted_at") private OffsetDateTime deletedAt;
 protected DireccionEnvio(){} public DireccionEnvio(Long cuentaId){this.cuentaId=cuentaId;principal=true;createdAt=OffsetDateTime.now();updatedAt=createdAt;}
 public void update(String alias,String destinatario,String calle,String numero,String piso,String codigoPostal,String localidad,String provincia,String pais,String telefono){this.alias=alias;this.destinatario=destinatario;this.calle=calle;this.numero=numero;this.piso=piso;this.codigoPostal=codigoPostal;this.localidad=localidad;this.provincia=provincia;this.pais=pais;this.telefono=telefono;updatedAt=OffsetDateTime.now();}
 public void makePrincipal(){principal=true;updatedAt=OffsetDateTime.now();} public void clearPrincipal(){principal=false;updatedAt=OffsetDateTime.now();} public void delete(){principal=false;deletedAt=OffsetDateTime.now();updatedAt=deletedAt;}
 public Long getId(){return id;} public Long getCuentaId(){return cuentaId;} public String getAlias(){return alias;} public String getDestinatario(){return destinatario;} public String getCalle(){return calle;} public String getNumero(){return numero;} public String getPiso(){return piso;} public String getCodigoPostal(){return codigoPostal;} public String getLocalidad(){return localidad;} public String getProvincia(){return provincia;} public String getPais(){return pais;} public String getTelefono(){return telefono;} public Boolean getPrincipal(){return principal;} public OffsetDateTime getDeletedAt(){return deletedAt;}
}
