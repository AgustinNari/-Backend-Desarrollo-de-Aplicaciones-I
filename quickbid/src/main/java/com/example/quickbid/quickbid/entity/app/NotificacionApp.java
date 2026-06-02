package com.example.quickbid.quickbid.entity.app;
import java.time.OffsetDateTime; import jakarta.persistence.*;
@Entity @Table(name="app_notificaciones")
public class NotificacionApp {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="cuenta_id",nullable=false) private Long cuentaId; @Column(nullable=false) private String tipo;
 @Column(nullable=false) private String titulo; @Column(nullable=false) private String descripcion;
 @Column(name="referencia_tipo") private String referenciaTipo; @Column(name="referencia_id") private Long referenciaId;
 @Column(nullable=false) private Boolean leida; @Column(name="created_at",nullable=false) private OffsetDateTime createdAt; @Column(name="read_at") private OffsetDateTime readAt;
 protected NotificacionApp(){} public NotificacionApp(Long cuenta,String tipo,String titulo,String descripcion,String referenciaTipo,Long referenciaId){cuentaId=cuenta;this.tipo=tipo;this.titulo=titulo;this.descripcion=descripcion;this.referenciaTipo=referenciaTipo;this.referenciaId=referenciaId;leida=false;createdAt=OffsetDateTime.now();} public Long getId(){return id;} public Long getCuentaId(){return cuentaId;} public String getTipo(){return tipo;} public String getTitulo(){return titulo;} public String getDescripcion(){return descripcion;} public String getReferenciaTipo(){return referenciaTipo;} public Long getReferenciaId(){return referenciaId;} public Boolean getLeida(){return leida;} public OffsetDateTime getCreatedAt(){return createdAt;}
 public void markRead(){if(!leida){leida=true;readAt=OffsetDateTime.now();}}
}
