package com.example.quickbid.quickbid.controller.usuario;
import java.util.Map; import jakarta.validation.Valid; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
import com.example.quickbid.quickbid.dto.request.DireccionEnvioRequest; import com.example.quickbid.quickbid.dto.response.ApiResponse; import com.example.quickbid.quickbid.dto.response.UsuarioDtos.*; import com.example.quickbid.quickbid.service.UsuarioService;
@RestController @RequestMapping("/api/usuario") public class UsuarioController {
 private final UsuarioService usuarios; public UsuarioController(UsuarioService u){usuarios=u;}
 @GetMapping("/perfil") public ApiResponse<Profile> profile(Authentication a){return ApiResponse.success(usuarios.profile(id(a)),"Perfil");}
 @GetMapping("/estadisticas") public ApiResponse<Statistics> statistics(Authentication a,@RequestParam(defaultValue="total")String periodo){return ApiResponse.success(usuarios.statistics(id(a),periodo),"Estadisticas");}
 @GetMapping("/historial") public ApiResponse<Page<HistoryItem>> history(Authentication a,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ApiResponse.success(usuarios.history(id(a),page,size),"Historial");}
 @GetMapping("/notificaciones") public ApiResponse<Page<Notification>> notifications(Authentication a,@RequestParam(required=false)String tipo,@RequestParam(required=false)String categoria,@RequestParam(required=false)Boolean leida,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ApiResponse.success(usuarios.notifications(id(a),tipo!=null?tipo:categoria,leida,page,size),"Notificaciones");}
 @PatchMapping("/notificaciones/all/leer") public ApiResponse<java.util.List<Notification>> readAll(Authentication a){return ApiResponse.success(usuarios.readAllNotifications(id(a)),"Notificaciones leidas");}
 @PatchMapping("/notificaciones/{id}/leer") public ApiResponse<Notification> read(Authentication a,@PathVariable Long id){return ApiResponse.success(usuarios.readNotification(id(a),id),"Notificacion leida");}
 @GetMapping("/direccion-envio") public ApiResponse<Address> address(Authentication a){return ApiResponse.success(usuarios.address(id(a)),"Direccion de envio");}
 @PutMapping("/direccion-envio") public ApiResponse<Address> updateAddress(Authentication a,@Valid @RequestBody DireccionEnvioRequest request){return ApiResponse.success(usuarios.updateAddress(id(a),request),"Direccion de envio actualizada");}
 @GetMapping("/direcciones-envio") public ApiResponse<java.util.List<Address>> addresses(Authentication a){return ApiResponse.success(usuarios.addresses(id(a)),"Direcciones de envio");}
 @PostMapping("/direcciones-envio") public ApiResponse<Address> createAddress(Authentication a,@Valid @RequestBody DireccionEnvioRequest request){return ApiResponse.success(usuarios.createAddress(id(a),request),"Direccion de envio creada");}
 @DeleteMapping("/direcciones-envio/{addressId}") public ApiResponse<Void> deleteAddress(Authentication a,@PathVariable Long addressId){usuarios.deleteAddress(id(a),addressId);return ApiResponse.success(null,"Direccion de envio eliminada");}
 @PatchMapping("/direcciones-envio/{addressId}/principal") public ApiResponse<Address> principalAddress(Authentication a,@PathVariable Long addressId){return ApiResponse.success(usuarios.setPrincipalAddress(id(a),addressId),"Direccion principal actualizada");}
 private Long id(Authentication authentication){return (Long)authentication.getPrincipal();}
}
