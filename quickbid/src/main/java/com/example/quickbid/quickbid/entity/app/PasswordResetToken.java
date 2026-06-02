package com.example.quickbid.quickbid.entity.app;
import java.time.OffsetDateTime; import jakarta.persistence.*;
@Entity @Table(name="app_password_reset_tokens")
public class PasswordResetToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="cuenta_id",nullable=false) private Long cuentaId; @Column(name="token_hash",nullable=false,unique=true) private String tokenHash;
 @Column(name="expires_at",nullable=false) private OffsetDateTime expiresAt; @Column(name="used_at") private OffsetDateTime usedAt;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 protected PasswordResetToken(){} public PasswordResetToken(Long id,String hash){cuentaId=id;tokenHash=hash;createdAt=OffsetDateTime.now();expiresAt=createdAt.plusMinutes(30);}
 public Long getCuentaId(){return cuentaId;} public boolean valid(){return usedAt==null&&expiresAt.isAfter(OffsetDateTime.now());} public void use(){usedAt=OffsetDateTime.now();}
}
