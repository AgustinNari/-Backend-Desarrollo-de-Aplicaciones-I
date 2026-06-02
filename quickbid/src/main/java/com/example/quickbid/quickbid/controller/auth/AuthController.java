package com.example.quickbid.quickbid.controller.auth;
import java.util.Map; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile;
import com.example.quickbid.quickbid.dto.response.ApiResponse; import com.example.quickbid.quickbid.service.AuthService; import com.fasterxml.jackson.annotation.JsonProperty;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final AuthService auth; public AuthController(AuthService a){auth=a;}
 @PostMapping("/registro/etapa1") public ResponseEntity<ApiResponse<Map<String,Object>>> etapa1(@Valid @RequestBody Etapa1 r){return ResponseEntity.status(201).body(ApiResponse.success(auth.etapa1(r.email,r.nombre,r.apellido,r.domicilioLegal,r.idPaisOrigen),"Solicitud creada"));}
 @PostMapping(value="/registro/etapa2",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ApiResponse<Void> etapa2(@RequestParam String email,@RequestPart MultipartFile fotoFrente,@RequestPart MultipartFile fotoDorso){auth.etapa2(email,fotoFrente,fotoDorso);return ApiResponse.success(null,"Documentacion recibida");}
 @PostMapping("/registro/verificar-token") public ApiResponse<Void> verify(@Valid @RequestBody Token r){auth.verify(r.token);return ApiResponse.success(null,"Token valido");}
 @PostMapping("/registro/etapa3") public ApiResponse<Map<String,Object>> etapa3(@Valid @RequestBody PasswordToken r){return ApiResponse.success(auth.etapa3(r.token,r.clave),"Cuenta creada");}
 @PostMapping("/registro/reenviar-link") public ApiResponse<Void> resend(@Valid @RequestBody EmailRequest r){auth.resend(r.email);return ApiResponse.success(null,"Si corresponde, se envio un nuevo enlace");}
 @PostMapping("/login") public ApiResponse<Map<String,Object>> login(@Valid @RequestBody Login r){return ApiResponse.success(auth.login(r.email,r.clave),"Login exitoso");}
 @PostMapping("/refresh") public ApiResponse<Map<String,Object>> refresh(@Valid @RequestBody Refresh r){return ApiResponse.success(auth.refresh(r.refreshToken),"Token renovado");}
 @PostMapping("/logout") public ApiResponse<Void> logout(@Valid @RequestBody Refresh r){auth.logout(r.refreshToken);return ApiResponse.success(null,"Sesion cerrada");}
 @PostMapping("/recuperar-clave") public ApiResponse<Void> recover(@Valid @RequestBody EmailRequest r){auth.recover(r.email);return ApiResponse.success(null,"Si el email existe, se envio un enlace");}
 @PutMapping("/cambiar-clave") public ApiResponse<Void> change(@Valid @RequestBody PasswordToken r){auth.change(r.token,r.clave);return ApiResponse.success(null,"Clave actualizada");}
 @GetMapping("/sesion") public ApiResponse<Map<String,Object>> session(Authentication a){return ApiResponse.success(auth.session((Long)a.getPrincipal()),"Sesion actual");}
 public record Etapa1(@Email @NotBlank String email,@NotBlank String nombre,@NotBlank String apellido,@NotBlank String domicilioLegal,@NotNull Integer idPaisOrigen){}
 public record EmailRequest(@Email @NotBlank String email){} public record Login(@Email @NotBlank String email,@NotBlank String clave){}
 public record Token(@NotBlank String token){} public record PasswordToken(@NotBlank String token,@NotBlank String clave){}
 public record Refresh(@JsonProperty("refreshToken") @NotBlank String refreshToken){}
}
