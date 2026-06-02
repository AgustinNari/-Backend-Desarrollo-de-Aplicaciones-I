package com.example.quickbid.quickbid.controller.usuario;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile;
import com.example.quickbid.quickbid.dto.request.MedioPagoRequest; import com.example.quickbid.quickbid.dto.response.*; import com.example.quickbid.quickbid.service.MedioPagoService;
@RestController @RequestMapping("/api/usuario/medios-pago") public class MedioPagoController {
 private final MedioPagoService medios; public MedioPagoController(MedioPagoService m){medios=m;}
 @GetMapping public ApiResponse<List<MedioPagoResponse>> list(Authentication a){return ApiResponse.success(medios.list(id(a)),"Medios de pago");}
 @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE) public ResponseEntity<ApiResponse<MedioPagoResponse>> create(Authentication a,@Valid @RequestBody MedioPagoRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(medios.create(id(a),r),"Medio de pago pendiente de verificacion"));}
 @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<ApiResponse<MedioPagoResponse>> cheque(Authentication a,@RequestParam String moneda,@RequestParam Boolean nacional,@RequestParam String titular,@RequestParam String numeroCheque,@RequestParam BigDecimal monto,@RequestParam LocalDate fechaVencimiento,@RequestParam String bancoEmisor,@RequestPart(required=false) MultipartFile fotoAnverso,@RequestPart(required=false) MultipartFile fotoReverso){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(medios.createCheque(id(a),moneda,nacional,titular,numeroCheque,monto,fechaVencimiento,bancoEmisor,fotoAnverso,fotoReverso),"Cheque pendiente de verificacion"));}
 @DeleteMapping("/{id}") public ApiResponse<Void> delete(Authentication a,@PathVariable Long id){medios.delete(id(a),id);return ApiResponse.success(null,"Medio de pago eliminado");}
 @PatchMapping("/{id}/principal") public ApiResponse<MedioPagoResponse> principal(Authentication a,@PathVariable Long id){return ApiResponse.success(medios.principal(id(a),id),"Medio principal actualizado");}
 private Long id(Authentication a){return (Long)a.getPrincipal();}
}
