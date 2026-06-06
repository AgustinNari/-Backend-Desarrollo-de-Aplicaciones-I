package com.example.quickbid.quickbid.dto.response;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.List;
public final class UsuarioDtos {
 private UsuarioDtos(){}
 public record CategoryProgress(String categoriaActual,String siguienteCategoria,Integer puntosActuales,Integer puntosSiguienteCategoria,Integer puntosFaltantes,Integer porcentaje){}
 public record Permissions(boolean puedeNavegar,boolean puedePujar,boolean puedeInscribirse,boolean puedeConsignar,boolean tieneRestriccionMulta){}
 public record Profile(Long cuentaId,String nombre,String apellido,String email,String categoria,Integer puntos,CategoryProgress progreso,String estadoCuenta,String estadoOperativo,Permissions permisos){}
 public record MonthlyActivity(String mes,Integer pujas,Integer compras){}
 public record BuyerMetrics(BigDecimal totalPujado,BigDecimal totalPagado,BigDecimal tasaExito,Integer cantidadCompras,Integer cantidadPujas,Integer subastasParticipadas){}
 public record SellerMetrics(Integer consignaciones,Integer vendidas,Integer liquidadas,BigDecimal totalLiquidado){}
 public record Statistics(String periodo,BigDecimal totalPujado,BigDecimal totalPagado,BigDecimal tasaExito,Integer cantidadCompras,Integer cantidadPujas,Integer subastasParticipadas,BuyerMetrics compradorPostor,SellerMetrics vendedorConsignador,List<MonthlyActivity> actividadMensual){}
 public record HistoryItem(String tipo,Integer subastaId,Integer itemCatalogoId,Integer productoId,BigDecimal monto,String moneda,OffsetDateTime fecha,String estado){}
 public record Page<T>(List<T> content,int page,int size,long totalElements,int totalPages){}
 public record Notification(Long id,String tipo,String titulo,String descripcion,String referenciaTipo,Long referenciaId,Boolean leida,OffsetDateTime createdAt){}
 public record Address(Long id,String alias,String destinatario,String calle,String numero,String piso,String codigoPostal,String localidad,String provincia,String pais,String telefono,Boolean principal){}
}
