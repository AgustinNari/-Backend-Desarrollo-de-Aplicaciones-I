# API_CONTRATO_FINAL — QuickBid

Este archivo define el contrato final que debe consumir el frontend. Si hay diferencias con `docs/03_api_contract.md` o `source_material/endpoints_originales.md`, prevalece este archivo.

## Convenciones generales

- Base path: `/api`.
- Salvo auth, registro y vistas públicas/invitado, todos los endpoints requieren `Authorization: Bearer <access_token>`.
- Los endpoints admin son auxiliares y no tienen frontend formal.
- Uploads: `multipart/form-data`.
- Respuesta estándar:

```json
{ "data": {}, "message": "Mensaje descriptivo", "errors": null }
```

Error:

```json
{
  "data": null,
  "message": "No se pudo procesar la solicitud",
  "errors": [{ "field": "campo", "code": "CODIGO_ERROR", "message": "Detalle" }]
}
```

## Catalogos publicos

No requieren token porque se usan durante el registro.

- `GET /api/catalogos/paises?q=&buscar=&page=0&size=50`
- `GET /api/catalogos/paises/{id}`

`q` y `buscar` son alias opcionales. La busqueda es case-insensitive sobre
nombre, nombre corto, nacionalidad y capital. Sin busqueda, los paises se
devuelven paginados y ordenados por nombre. `id` es el alias frontend-friendly
de `paises.numero`.

```json
{
  "data": {
    "content": [
      {
        "id": 32,
        "nombre": "Argentina",
        "nombreCorto": "AR",
        "nacionalidad": "argentina"
      }
    ],
    "page": 0,
    "size": 50,
    "totalElements": 1,
    "totalPages": 1
  },
  "message": "Paises",
  "errors": []
}
```

## Auth y registro

### `POST /api/auth/registro/etapa1`

```json
{
  "email": "usuario@email.com",
  "nombre": "Nombre",
  "apellido": "Apellido",
  "domicilioLegal": "Texto domicilio",
  "idPaisOrigen": 32
}
```

### `POST /api/auth/registro/etapa2`

Multipart: `email`, `fotoFrenteDni`, `fotoDorsoDni`.

Reglas: DNI solo imágenes; JPG/JPEG y PNG mínimos; WebP opcional; no PDF; frente y dorso obligatorios.

### `POST /api/auth/registro/verificar-token`

```json
{ "token": "..." }
```

### `POST /api/auth/registro/etapa3`

```json
{ "setup_token": "...", "clave": "...", "claveConfirmacion": "..." }
```

`setup_token`: 48 horas, un solo uso.

### `POST /api/auth/registro/reenviar-link`

```json
{ "email": "usuario@email.com" }
```

Respuesta genérica. Invalida link anterior si aplica.

### `POST /api/auth/login`

```json
{ "email": "usuario@email.com", "clave": "..." }
```

Respuesta normal:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "estadoCuenta": "activa",
  "usuario": { "id": 1, "email": "usuario@email.com", "nombre": "Nombre", "apellido": "Apellido", "categoria": "comun", "puntos": 0 }
}
```

Para `bloqueada_permanente`, permitir login limitado y devolver `estadoCuenta=bloqueada_permanente`; esa sesión no habilita navegación real.

### `POST /api/auth/refresh`

Refresh token rotativo, 30 días.

### `POST /api/auth/logout`

Revoca refresh/session.

### `POST /api/auth/recuperar-clave`

```json
{ "email": "usuario@email.com" }
```

Respuesta genérica. Token: 30 minutos, un solo uso.

### `PUT /api/auth/cambiar-clave`

Recuperación por token:

```json
{ "token": "...", "claveNueva": "...", "claveConfirmacion": "..." }
```

Cambio desde sesión activa:

```json
{ "claveActual": "...", "claveNueva": "...", "claveConfirmacion": "..." }
```

## Usuario

- `GET /api/usuario/perfil`
- `GET /api/usuario/estadisticas?periodo=mes|trimestre|anual|total`
- `GET /api/usuario/historial?page=0&size=20`

`periodo` debe aplicarse realmente. Estadísticas deben separar comprador/postor y vendedor/consignador si es viable. Historial incluye pujas ganadas, perdidas y superadas.

## Direcciones de envío

- `GET /api/usuario/direcciones-envio`
- `POST /api/usuario/direcciones-envio`
- `DELETE /api/usuario/direcciones-envio/{id}`
- `PATCH /api/usuario/direcciones-envio/{id}/principal`

Máximo 5 activas, una principal, baja lógica, evitar edición destructiva.

## Notificaciones

- `GET /api/usuario/notificaciones?categoria=&leida=&page=&size=`
- `PATCH /api/usuario/notificaciones/{id}/leer`

Para marcar todas como leídas, puede aceptarse `{id}=all` si no complica. Si no, se permite endpoint separado `/api/usuario/notificaciones/all/leer` documentado como excepción.

## Medios de pago

- `GET /api/usuario/medios-pago`
- `POST /api/usuario/medios-pago`
- `DELETE /api/usuario/medios-pago/{id}`
- `PATCH /api/usuario/medios-pago/{id}/principal`

Tipos: `tarjeta`, `cuenta_bancaria`, `cheque_certificado`. Monedas: `ARS`, `USD`.

Reglas: nuevo medio queda `pendiente_verificacion`; no usable hasta verificación salvo inscripción/revalidación; validación/revalidación admin exige `limiteAprobado`; principal único por moneda.

## Subastas

### Públicos

- `GET /api/subastas`
- `GET /api/subastas/{id}`
- `GET /api/subastas/{id}/catalogo`
- `GET /api/items/{id}`

Invitado no ve precio base, mejor oferta, cantidad/historial de pujas, item vivo ni estado real de lote.

### Protegidos

- `POST /api/subastas/{id}/inscribirse`
- `POST /api/subastas/{id}/verificacion`
- `GET /api/subastas/{id}/puja-actual`
- `POST /api/subastas/{id}/pujar`

Inscripción:

```json
{ "idMedioPago": 123 }
```

Reglas: hasta 60 minutos antes; acepta medio `pendiente_verificacion`, `vencido` o `verificado`; inscripción rechazada permite nuevo intento; no crea asistencia.

Puja:

```json
{
  "idItem": 1,
  "valorOfertado": 10000,
  "idMedioPago": 1,
  "clientStateVersion": 8,
  "idempotencyKey": "uuid"
}
```

`idempotencyKey` obligatorio. Primera puja puede ser precio base. Oro/platino superan por 1 unidad. Resto: mínimo +1%, máximo +20%. No pujar producto propio. No tener puja ganadora activa en otra subasta. Reservar límite mientras es ganadora, liberar si es superada y convertir la reserva en consumo real si gana el lote.

## Compras

- `GET /api/compras?estado=...`
- `GET /api/compras/{id}`
- `PUT /api/compras/{id}/entrega`
- `POST /api/compras/{id}/pagar`
- `POST /api/compras/{id}/pagar-con-multa`
- `GET /api/compras/{id}/documentos`

`/pagar`: comisiones/envío/extras. `/pagar-con-multa`: artículo + multa juntos. Dirección se congela al pagar extras. Factura cuando compra queda completamente pagada. Recibo de multa cuando se paga multa.

## Consignaciones

- `GET /api/consignaciones/requisitos`
- `POST /api/consignaciones`
- `POST /api/consignaciones/{id}/documentacion-origen`
- `GET /api/consignaciones?filtro=activas|rechazadas|vendidas`
- `GET /api/consignaciones/{id}`
- `POST /api/consignaciones/{id}/acuerdo/aceptar`
- `POST /api/consignaciones/{id}/acuerdo/rechazar`
- `POST /api/consignaciones/{id}/devolucion`
- `POST /api/consignaciones/{id}/devolucion/pagar-envio`

Crear consignación: mínimo 6 fotos, máximo 15. `segmento` es rubro/tema; `categoriaSubasta` es categoría común/especial/plata/oro/platino. Documentación de origen vuelve a revisión manual, preferentemente `pendiente_revision`.

Aceptar acuerdo:

```json
{ "leyoContrato": true, "aceptaClausulasPlazos": true }
```

Ambos deben ser `true`. Producto legacy se crea solo al aceptar acuerdo; luego no se rechaza.

## Admin auxiliar

Sin frontend formal. Debe permitir aprobar/rechazar usuarios, validar/rechazar/revalidar medios con `limiteAprobado`, ajustar puntos, gestionar subastas/lotes, procesar vencimientos, gestionar consignaciones, publicar/liquidar, y reenviar/regenerar documentos si se implementa.

## Estados mínimos expuestos

Cuenta: `activa`, `restriccion_multa`, `bloqueada_permanente`, `deshabilitada_admin`.

Medio: `pendiente_verificacion`, `verificado`, `rechazado`, `vencido`, `eliminado`.

Compra sugeridos: `adjudicacion_pendiente`, `multa_activa`, `pagos_extra_pendientes`, `pagada`, `entrega_pendiente`, `retiro_pendiente`, `abandonada_por_incumplimiento_pago`, `abandonada_por_incumplimiento_retiro`, `completada`.

Consignación sugeridos: `pendiente_revision`, `rechazo_inicial`, `documentacion_adicional`, `recepcion_pendiente`, `revision_fisica`, `rechazo_revision_fisica`, `acuerdo_pendiente`, `acuerdo_aceptado`, `acuerdo_rechazado`, `devolucion_pendiente`, `publicada`, `en_subasta`, `vendida`, `comprada_por_empresa`, `liquidada`, `devolucion_incompleta`.
