# 08 — Validaciones, errores, seguridad, archivos y auditoría

## Seguridad

- Usar JWT para access token.
- Usar refresh token persistido y revocable si se implementa sesión persistente.
- Hashear passwords con BCrypt/Argon2.
- Hashear tokens temporales en base de datos; nunca guardar tokens planos.
- No exponer si un email existe en recuperación o reenvío de link.
- Rate limit en login, recuperación y reenvío.
- Validar propiedad de recursos en todos los endpoints (`403` si no pertenece al usuario).
- Sanitizar nombres de archivo y validar MIME real además de extensión.
- No guardar números completos de tarjeta si no hay cumplimiento PCI. Guardar token simulado/hash/últimos 4 para pruebas.

## Validaciones de backend obligatorias

### Registro

- email válido y único;
- país existe en `paises`;
- DNI frente/dorso presentes;
- archivo permitido y tamaño máximo;
- token vigente, un solo uso;
- password cumple reglas.

### Cuenta

- estado permite login/navegación;
- bloqueos/restricciones aplican en acciones económicas;
- sesión refresh revocable.

### Medios de pago

- campos requeridos según tipo;
- moneda válida;
- tarjeta con formato básico/Luhn si se desea;
- CVV válido en formato;
- cheque con monto > 0, vencimiento válido, fotos requeridas;
- cuenta bancaria con CBU/CVU/alias según decisión;
- duplicados por hash identificador;
- no permitir eliminar si asociado a operación pendiente.
- siempre borrado lógico para medios de pago.

### Subastas/pujas

- categoría usuario >= categoría subasta;
- medio de pago verificado;
- moneda del medio = moneda subasta;
- cuenta sin multa/restricción;
- un usuario no puede estar conectado/participando activamente en más de una subasta a la vez;
- dentro de la misma subasta, un usuario puede participar en los lotes que se vayan activando, respetando las reglas de puja;
- monto mayor a mejor oferta;
- límites 1%/20% para categorías no oro/platino;
- fondos/límites suficientes;
- control de concurrencia.

### Compras/pagos

- compra pertenece al usuario;
- compra está en estado pagable;
- medio de pago válido y moneda coincidente;
- cálculo correcto de multa 10%;
- plazo 72h para fondos/multa;
- no pagar dos veces por concurrencia.

### Consignación

- al menos 6 fotos;
- declaración de propiedad obligatoria;
- aceptación de devolución con cargo obligatoria;
- para iniciar, cuenta bancaria registrada y no rechazada/eliminada;
- para iniciar, al menos un medio de pago registrado y no rechazado/eliminado;
- para iniciar, no exigir `verificado_hasta` vigente;
- para pagar envío de devolución, exigir medio verificado con
  `verificado_hasta` vigente, moneda compatible y fondos/límite;
- para liquidar, exigir cuenta bancaria destino no eliminada sin exigir
  verificación manual vigente;
- documentación de origen si se requiere;
- no crear `productos` sin `duenios`, revisor y acuerdo aceptado;
- no rechazar/devolver luego de acuerdo aceptado si la regla definida indica que ya pasa a circuito definitivo.

`verificado_hasta` representa la vigencia temporal de la validación manual de
empresa. No debe confundirse con el vencimiento propio de una tarjeta.

## Errores estándar sugeridos

```json
{
  "data": null,
  "message": "No se pudo procesar la solicitud",
  "errors": [
    { "field": "monto", "code": "BID_TOO_LOW", "message": "La puja debe superar el mínimo permitido" }
  ]
}
```

Códigos internos útiles:

- `AUTH_INVALID_CREDENTIALS`
- `ACCOUNT_PENDING_APPROVAL`
- `ACCOUNT_BLOCKED`
- `ACCOUNT_RESTRICTED_BY_FINE`
- `PAYMENT_METHOD_NOT_VERIFIED`
- `PAYMENT_METHOD_CURRENCY_MISMATCH`
- `AUCTION_CATEGORY_FORBIDDEN`
- `BID_TOO_LOW`
- `BID_TOO_HIGH`
- `BID_OUTDATED_STATE`
- `INSUFFICIENT_FUNDS_OR_LIMIT`
- `RESOURCE_NOT_OWNED`
- `INVALID_STATE_TRANSITION`
- `FILE_TYPE_NOT_SUPPORTED`

## Auditoría mínima

Registrar:

- creación/aprobación/rechazo de registro;
- login fallidos repetidos y bloqueos;
- cambios de password;
- verificación/rechazo de medios de pago;
- inscripción a subasta;
- cada puja aceptada/rechazada por backend;
- cierre de lote;
- creación de compra;
- pago exitoso/fallido;
- creación/vencimiento/pago de multa;
- cambios de estado de cuenta;
- consignación y cambios de estado;
- creación de `duenios` y `productos` desde consignación;
- acciones admin.

## Archivos

- Usar `multipart/form-data`.
- Guardar metadata en DB y binario en storage local/cloud.
- Calcular checksum para evitar duplicados si ayuda.
- No servir archivos sin validar permisos.
- Imágenes de productos pueden tener endpoint público solo si no exponen información sensible.

## Respuestas y DTOs

Crear DTOs separados para:

- invitado;
- usuario autenticado;
- admin;
- detalle vs listado;
- request vs response.

Nunca devolver entidades JPA directamente.

---

## Addendum post revisión de validaciones

- `setup_token` obligatorio en etapa 3.
- DNI no acepta PDF y requiere `fotoFrenteDni`/`fotoDorsoDni`.
- Cuenta bloqueada permanente solo obtiene sesión limitada.
- Verificar/revalidar medio exige `limiteAprobado > 0`.
- Medio vencido/verificado puede revalidarse reiniciando 5 días hábiles.
- Inscripción cierra 60 minutos antes y rechazada no bloquea reintento con otro medio.
- `idempotencyKey` obligatorio en puja.
- Validar reserva/liberación/consumo de límite.
- Prohibir puja por producto propio.
- Comisión sobre precio final ofertado.
- Compra empresa sin comisión comprador.
- Consignación: mínimo 6 y máximo 15 fotos; acuerdo requiere checkboxes; `duenio` obligatorio antes de proponer acuerdo, no al iniciar.
