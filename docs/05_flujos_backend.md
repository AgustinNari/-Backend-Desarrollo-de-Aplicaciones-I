# 05 — Flujos backend esperados

## 1. Registro de usuario

### Etapa 1

1. Validar email, nombre, apellido, domicilio legal, país.
2. Verificar que email no exista en `app_cuentas` ni solicitud activa incompatible.
3. Crear/actualizar `app_solicitudes_registro` en estado `pendiente_etapa2`.
4. Responder `idRegistro` y siguiente paso.

### Etapa 2

1. Buscar solicitud por email.
2. Validar estado y archivos.
3. Guardar archivos en `app_archivos`.
4. Vincular frente/dorso DNI.
5. Cambiar a `pendiente_revision`.
6. Notificar/registrar auditoría.

### Aprobación manual admin

1. Admin/verificador aprueba solicitud.
2. En transacción crear `personas` y `clientes` legacy.
3. Asignar país a `clientes.numeroPais`.
4. Asignar categoría inicial manual.
5. Generar token de setup hash, 48h, un solo uso.
6. Marcar `aprobada_pendiente_finalizacion`.
7. Enviar mail o log simulado con link.

### Etapa 3

1. Validar setup token.
2. Validar password.
3. Crear `app_cuentas` asociada a `personas`/`clientes`.
4. Guardar hash password.
5. Marcar solicitud `completada` y token usado.
6. Emitir access/refresh tokens.

## 2. Login

1. Buscar cuenta por email.
2. Si no existe, responder genérico según contrato.
3. Validar password.
4. Si cuenta `bloqueada_permanente` o `deshabilitada_admin`, devolver `403` con estado para pantalla de bloqueo.
5. Si `activa` o `restriccion_multa`, login OK; la restricción se aplica por permisos de acciones económicas.
6. Emitir tokens.

## 3. Modo invitado

Para endpoints públicos de subastas/catálogos:

- detectar ausencia de token;
- devolver DTO reducido;
- nunca incluir precios, puja actual, historial, lote activo, montos ni estado live.

Para endpoints protegidos:

- ausencia de token -> `401`;
- usuario invitado simulado -> `403` si se implementa token guest.

## 4. Alta de medio de pago

1. Validar usuario activo.
2. Validar tipo y campos específicos.
3. Guardar archivos si es cheque.
4. Guardar medio en `pendiente_verificacion`.
5. No sumar puntos todavía.
6. Admin verifica/rechaza.
7. Al verificar: sumar puntos, permitir uso, notificar.

## 5. Inscripción a subasta (De cierta manera tambien como modo de revalidar un metodo de pago)

1. Validar cuenta activa y sin `restriccion_multa`.
2. Validar categoría usuario >= categoría subasta.
3. Validar medio de pago de misma moneda.
4. Crear `app_inscripciones_subasta` 

## 6. Puja

1. Requiere token, cuenta activa, sin restricción.
2. Validar subasta/lote abierto.
3. No exigir inscripcion previa: es interes/revalidacion, no permiso de puja.
4. Validar medio de pago verificado, moneda y límites.
5. Obtener estado vivo con lock optimista/pesimista.
6. Calcular mejor oferta actual.
7. Validar monto mínimo/máximo según categoría.
8. Validar fondos/garantía/límite.
9. Insertar puja con secuencia.
10. Marcar puja anterior como `superada`.
11. Actualizar estado vivo y versionado.
12. Emitir WebSocket: nueva puja, puja superada, estado actualizado.
13. Adicionalmente, si es su primera puja en esa subasta, crear registro en `asistentes` legacy y asignar `numeroPostor` respetando regla de unicidad (Recordando que la asistencia efectiva está determinada por una puja confirmada en cualquier artículo de una subasta).

## 7. Cierre de lote/subasta

1. Determinar mejor puja aceptada.
2. Si existe, marcar como `ganadora`.
3. Crear compra para usuario.
4. Si no existe, crear compra interna de empresa por precio base.
5. Marcar producto/item vendido o sin venta según tabla legacy/extendida.
6. Calcular comisiones y documentos preliminares.
7. Notificar comprador/consignador.

## 8. Pago de puja ganadora (Realizado de manera automática por el sistema al mismo momento en que se confirma dicha puja como ganadora)

1. Validar puja/compra pertenece al usuario.
2. Validar estado pagable.
3. Validar medio de pago y moneda.
4. Simular/ejecutar cobro.
5. Si éxito: puja/compra `pagada`, pago `aprobado`, sumar puntos, pasar a entrega.
6. Si falla por fondos/excepción: crear multa 10%, compra `multa_generada`, cuenta `restriccion_multa`, notificar.

## 9. Pago con multa

1. Validar multa pendiente y compra asociada.
2. Cobrar multa + obligación pendiente si corresponde.
3. Si éxito: marcar multa pagada, compra pagada, cuenta vuelve a `activa` si no tiene otras multas.
4. Si vence plazo 72h sin pago: cuenta `bloqueada_permanente`.

## 10. Entrega/retiro

1. Usuario elige envío o retiro.
2. Si envío, validar dirección y costo.
3. Si retiro, marcar que pierde cobertura de seguro al retirar.
4. Usuario paga los costos asociados de la comision (+ envio si aplica).
5. Gestionar estados hasta `entregada` o `retirada`.
6. Si el usaurio no gestiona en plazo los pagos adicinales al valor pujado, entonces la empresa se queda con el articulo/item.
7. Si el usaurio no gestiona en plazo la entrega/retiro, entonces la empresa se queda con el articulo/item.

## 11. Consignación

1. Usuario registra una cuenta bancaria destino y al menos un medio de pago,
   ambos no rechazados ni eliminados. No hace falta verificación manual vigente.
2. Usuario crea solicitud con datos, fotos y declaraciones.
3. Estado `enviada` o `revision_digital`.
4. Empresa puede pedir documentación de origen.
5. Si no interesa/rechaza en revisión digial: informar motivo y el proceso termina ahí.
6. Si interesa: pedir envío físico/inspección.
7. Si no se valida la revisión física: informar motivo y se deberá gestionar devolución con cargo.
8. Si se acepta en revisión física, luego se debe armar el acuerdo de consignación.
9. Antes de acuerdo, empresa valida consignador y crea `duenios` si no existe.
10. Empresa propone acuerdo con valor base, comisiones, subasta tentativa.
11. Usuario acepta o rechaza.
12. Si acepta: crear `productos` legacy, vincular `solicitudes_consignacion.producto_id`, fotos, seguro si aplica.
13. Publicar/asignar a subasta y catálogo.
14. Si se vende: liquidar a cuenta bancaria declarada, sin exigir verificación manual vigente.
15. Si se rechaza al realizar la revisión física o si el usuario rechaza el acuerdo: devolución con cargo.
16. Para envío de devolución, cobrar con un medio verificado y con
    `verificado_hasta` vigente elegido al pagar. Para retiro no cobrar envío.
17. Si no gestiona la devolución en plazo, entonces la empresa se queda con el articulo/item.

## 12. Notificaciones

Cada cambio de estado importante debe crear una notificación persistida y, si el usuario está conectado, emitir evento realtime.

---

## Addendum post revisión de flujos

- Login de `bloqueada_permanente`: limitado a pantalla informativa, sin navegación real.
- Inscripción: cierra 60 minutos antes, acepta medio pendiente/verificado/vencido, permite reintento si fue rechazada, no crea asistencia.
- Puja: `idempotencyKey` obligatorio, prohibido producto propio, reserva límite mientras es mejor oferta, libera reserva al ser superada y consume al ganar final.
- Cierre de lote: 60 segundos sin superación; luego 60 segundos hasta próximo lote; si era último lote, 120 segundos hasta finalizar subasta.
- Compras: comisión comprador/vendedor sobre precio final ofertado; compra empresa al precio base sin comisión comprador.
- Consignación: 6 a 15 fotos; documentación de origen vuelve a revisión manual; producto legacy solo al aceptar acuerdo; `duenio` obligatorio antes de proponer acuerdo.
