> **Contrato base/original:** para implementación actual y frontend, usar `docs/API_CONTRATO_FINAL.md`. Si hay contradicción, prevalece `API_CONTRATO_FINAL.md`.


# 03 — Contrato de API

## Convenciones generales

- Base path: `/api`.
- Salvo auth/registro y vistas públicas, todos los endpoints requieren `Authorization: Bearer <token>`.
- Respuesta JSON estándar:

```json
{
  "data": {},
  "message": "Texto descriptivo",
  "errors": []
}
```

- `errors` es opcional y puede incluir objetos `{ field, code, message }`.
- El éxito/error se comunica por HTTP status code.
- Para uploads usar `multipart/form-data`.
- Para paginación usar parámetros `page`, `size`, `sort` cuando aplique.
- Para filtros usar query params estables; no romper endpoints base.

## Autenticación y registro

### `POST /api/auth/registro/etapa1`

Primera etapa del registro: captura datos personales básicos.

Request:

```json
{
  "email": "usuario@email.com",
  "nombre": "Nombre",
  "apellido": "Apellido",
  "domicilioLegal": "Texto o estructura serializada",
  "idPaisOrigen": 32
}
```

Retorna `idRegistro` y siguiente paso.

Status:

- `201`: datos registrados correctamente.
- `400`: campos obligatorios faltantes.
- `409`: email ya registrado.
- `422`: país inválido o formato email incorrecto.

### `POST /api/auth/registro/etapa2`

Subida de DNI frente/dorso. Cuenta queda pendiente de revisión manual.

Multipart:

- `email`
- `fotoFrenteDni`
- `fotoDorsoDni`

Status:

- `200`: fotos recibidas, cuenta en revisión.
- `400`: falta foto o formato inválido.
- `404`: email no corresponde a etapa 1.
- `409`: fotos ya subidas.
- `422`: tamaño/formato no soportado.

### `POST /api/auth/registro/verificar-token`

Valida token del enlace enviado tras aprobación.

Request:

```json
{ "token": "..." }
```

Status:

- `200`: enlace válido.
- `400`: enlace inválido.
- `404`: sin código pendiente.
- `410`: token expiró.

### `POST /api/auth/registro/etapa3`

Crea clave personal y finaliza registro.

Request:

```json
{
  "setup_token": "...",
  "clave": "...",
  "claveConfirmacion": "..."
}
```

Status:

- `200`: registro finalizado, retorna sesión y usuario.
- `400`: clave inválida o no coincide.
- `401`: token temporal inválido/expirado.

### `POST /api/auth/registro/reenviar-link`

Solicita reenviar link para completar registro si aplica. Responder siempre genérico para evitar enumeración.

Request:

```json
{ "email": "usuario@email.com" }
```

Status:

- `200`: si el email es válido, se enviará enlace.
- `400`: email inválido.
- `429`: demasiados intentos.

### `POST /api/auth/login`

Request:

```json
{ "email": "usuario@email.com", "clave": "..." }
```

Status:

- `200`: login exitoso.
- `401`: email o clave incorrectos.
- `403`: cuenta bloqueada o pendiente de aprobación.
- `404`: email no registrado, aunque se recomienda unificar con `401` para seguridad si se decide.

### `POST /api/auth/recuperar-clave`

Request:

```json
{ "email": "usuario@email.com" }
```

Status:

- `200`: si el email existe se envía mail.
- `429`: más de 3 intentos en 15 minutos, si se implementa rate limit.

### `PUT /api/auth/cambiar-clave`

Desde recuperación o sesión activa.

Request:

```json
{
  "claveNueva": "...",
  "claveConfirmacion": "...",
  "token": "opcional si recuperación"
}
```

Status:

- `200`: contraseña actualizada.
- `400`: clave inválida/no coincide.
- `401`: token inválido/expirado.

## Métodos de pago

### `GET /api/usuario/medios-pago`

Lista medios del usuario.

### `POST /api/usuario/medios-pago`

Registra tarjeta, cuenta bancaria o cheque. Queda `pendiente_verificacion`.

Campos base:

- `tipo`: `tarjeta`, `cuenta_bancaria`, `cheque_certificado`.
- `moneda`: `ARS`, `USD`.
- `nacional`: boolean.

Tarjeta:

- `nombreTitular`, `numeroTarjeta`, `vencimiento`, `cvv`, `titular`.

Cuenta:

- `numeroCuenta` o CBU/CVU, `nombreBanco`, `alias`, titularidad/CUIT-CUIL si se implementa.

Cheque:

- `monto`, `fechaVencimiento`, `numeroCheque`, `fotoAnverso`, `fotoReverso`, banco emisor.

Status:

- `201`: creado pendiente de verificación.
- `400`: campos faltantes/CVV inválido.
- `401`: token inválido.
- `409`: medio idéntico.
- `422`: foto no soportada.

### `DELETE /api/usuario/medios-pago/{id}`

Eliminación lógica.

- `200`: eliminado.
- `403`: no pertenece al usuario.
- `404`: no encontrado.
- `409`: asociado a operación pendiente.

### `PATCH /api/usuario/medios-pago/{id}/principal`

Marca principal y desactiva principal anterior.

## Perfil, notificaciones, ayuda y estadísticas

- `GET /api/usuario/perfil`
- `GET /api/usuario/estadisticas`
- `GET /api/usuario/historial`
- `GET /api/usuario/notificaciones`
- `PATCH /api/usuario/notificaciones/{id}/leer`

Recomendado agregar:

- `PUT /api/usuario/direccion-envio`
- `GET /api/usuario/direccion-envio`

si el frontend lo necesita para compras/envíos.

## Subastas

### `GET /api/subastas`

Lista subastas. Debe soportar modo invitado:

- invitado ve datos generales sin precios;
- autenticado aprobado puede ver más datos según permisos/categoría.

Filtros sugeridos:

- `estado`, `categoria`, `moneda`, `fechaDesde`, `fechaHasta`, `q`.

### `GET /api/subastas/{id}`

Detalle de subasta. Invitado sin precios/live.

### `GET /api/subastas/{id}/catalogo`

Catálogo. Invitado sin precio base. Registrado puede ver precio base si está aprobado.

### `GET /api/items/{id}`

Detalle de ítem. Invitado sin precio base ni estado vivo.

### `POST /api/subastas/{id}/inscribirse`

Crea inscripción/asistencia del usuario si cumple requisitos.

Errores clave:

- `403`: categoría insuficiente, cuenta restringida, sin medio verificado.
- `409`: ya inscripto o conectado a otra subasta.
- `422`: moneda/medio incompatible.

### `POST /api/subastas/{id}/verificacion`

Valida si usuario puede acceder/pujar antes de entrar.

### `GET /api/subastas/{id}/puja-actual`

Estado vivo para usuario autorizado.

### `POST /api/subastas/{id}/pujar`

Request sugerido:

```json
{
  "itemCatalogoId": 123,
  "monto": 15100,
  "medioPagoId": 10,
  "clientStateVersion": 8
}
```

Status:

- `201`: puja registrada.
- `400`: monto inválido.
- `403`: no autorizado por categoría, cuenta, multa, medio.
- `409`: oferta superada/estado cambió/otra puja pendiente.
- `422`: fuera de límites, moneda incompatible, fondos insuficientes.

## Compras, pagos y entrega

- `GET /api/compras`
- `GET /api/compras/{id}`
- `PUT /api/compras/{id}/entrega`
- `POST /api/compras/{id}/pagar`
- `POST /api/compras/{id}/pagar-con-multa`
- `GET /api/compras/{id}/documentos`

- Consideración importante:

`POST /api/compras/{id}/pagar` se usa para pagar comisiones/envío/retiro o pagos extra pendientes.
El valor pujado se intenta cobrar automáticamente al cerrar el lote/adjudicar la compra.
Si ese cobro automático falla, se usa `POST /api/compras/{id}/pagar-con-multa` para pagar valor pujado + multa.

- Estados sugeridos compra:

`adjudicacion_pendiente`: ganó pero aún no se terminó de concretar el pago inmediato.
`multa_activa`: fallo el pago inmediato y se generó una multa.
`pagos_extra_pendientes`: pagó el valor pujado (y multa si aplica), pero todavía no las comisiones (y envío si aplica).
`pagada`: todos los pagos necesarios realizados.
`entrega_pendiente`: se pago todo y falta que llegue el bien al comprador.
`retiro_pendiente`: se pago todo y falta que el comprador retire el bien.
`abandonada_por_incumplimiento_pago`: ocurre cuando la empresa se queda con el bien por falta de pago de comisiones/envío.
`abandonada_por_incumplimiento_retiro`: ocurre cuando la empresa se queda con el bien por falta de retiro del mismo.
`completada`: el comprador ya tiene el bien en su posesión.

- Para estos estados de compra, al igual que se puede ver en el proceso general de consignación de debajo, se podría quizás separar en más de 1 enum, para conservar algunos estados en historial y tener una mejor gestión de los mismos.


## Consignaciones

- `GET /api/consignaciones/requisitos`
- `POST /api/consignaciones`
- `POST /api/consignaciones/{id}/documentacion-origen`
- `GET /api/consignaciones`
- `GET /api/consignaciones/{id}`
- `POST /api/consignaciones/{id}/acuerdo/aceptar`
- `POST /api/consignaciones/{id}/acuerdo/rechazar`
- `POST /api/consignaciones/{id}/devolucion`
- `POST /api/consignaciones/{id}/devolucion/pagar-envio`

- Estados sugeridos consignación:

`pendiente_revision`: submit hecho y la empresa está en proceso de revisarlo para la 1ra aprobación o rechazarlo directamente
`rechazo_inicial`: la empresa rechazó el artículo en la revisión “digital”.
`documentacion_adicional`: falta respaldo.
`recepcion_pendiente`: aceptado en fase inicial de validación “digital”, pero pendiente de que el usuario haga llegar el artículo a la empresa para la revisión física.
`revision_fisica`: bien recibido o en inspección.
`rechazo_revision_fisica`: la empresa rechazó el artículo en la revisión física.
`acuerdo_pendiente`: revisión física aprobada y empresa propuso condiciones.
`acuerdo_aceptado`: usuario aprobó.
`acuerdo_rechazado`: usuario rechazó.
`devolucion_pendiente`: la empresa tiene posesión del artículo pero no se puso en subasta por alguna razón, sea por parte de la empresa al rechazarlo en revisión física, o sea porque el usuario rechazó el acuerdo.
`publicada`: lista para subasta.
`en_subasta`: ya asignada.
`vendida`: se adjudicó a un cliente normal.
`comprada_por_empresa`: quedó sin pujas, entonces la empresa se hace cargo de adquirirlo al precio base.
`liquidada`: se pagó al dueño.
`devolucion_incompleta`: el usuario no fue a retirar el artículo, ni pagó el envío de devolución en plazo, por lo que la empresa puede hacer lo que vea acorde con el artículo recibido.

- Lo que se podría hacer adicionalmente, de manera similar como con las compras:
No usar un único enum gigante si después complica consultas, sino quizás mejor dividirlo en varias partes, separando los estados por ejes:
`estado_general`
`estado_revision`
`motivo_rechazo`
`estado_devolucion`
`estado_subasta`



## Endpoints auxiliares admin

Ver `docs/07_admin_auxiliar.md`.

## Nota

El archivo `source_material/endpoints_originales.md` conserva la especificación original completa de endpoints. Ante una duda fina, priorizar ese archivo.
