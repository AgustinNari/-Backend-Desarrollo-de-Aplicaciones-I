# Propuesta de actualización documental — 2026-06-06

Histórico de la respuesta usada como base.

---

Sí. Con tus respuestas, lo mejor es **no seguir dejando la verdad del proyecto repartida entre muchos `.txt`, `.docx`, `.md` y código**, porque Codex se puede confundir. Tus respuestas ya cambian varias cosas respecto de la auditoría anterior: por ejemplo, decidiste mantener la regla de inscripción de **60 minutos**, crear un contrato final único, permitir login limitado para cuentas bloqueadas, usar `setup_token`, aplicar puntos documentados, exigir límite aprobado al validar medios, separar `segmento` de `categoria`, etc.   

Mi recomendación es esta: **crear una capa nueva de documentación final, y después actualizar los `.md` existentes para que apunten a esa capa como fuente superior de verdad**.

---

# 1. Problema actual de documentación

En el ZIP ya tenés una buena estructura documental:

* `AGENTS.md`
* `INDEX.md`
* `README_BACKEND.md`
* `docs/01_contexto_negocio.md`
* `docs/02_reglas_negocio.md`
* `docs/03_api_contract.md`
* `docs/04_modelo_datos.md`
* `docs/05_flujos_backend.md`
* `docs/06_realtime_websocket.md`
* `docs/07_admin_auxiliar.md`
* `docs/08_validaciones_errores_seguridad.md`
* `docs/09_checklist_entrega.md`
* `quickbid/README.md`
* `quickbid/docs/CHECKLIST_FINAL.md`
* `source_material/*`

El problema es que `AGENTS.md` actualmente le dice a Codex que, si hay conflicto entre resumen y material original, use el material original. Eso ya no conviene, porque ahora tus respuestas nuevas son más recientes y resuelven contradicciones.

Entonces la jerarquía debería pasar a ser:

1. **`docs/00_decisiones_finales.md`**
   Fuente máxima para reglas de negocio finales.

2. **`docs/API_CONTRATO_FINAL.md`**
   Fuente máxima para endpoints, parámetros, DTOs y estados expuestos al frontend.

3. **`docs/10_plan_correcciones_backend.md`**
   Lista de correcciones concretas que Codex debe aplicar al código actual.

4. Documentación existente `docs/01` a `docs/09`, actualizada para no contradecir lo anterior.

5. `source_material/` solo como historial original, no como fuente final.

---

# 2. Archivos `.md` nuevos que recomiendo crear

## 2.1. Crear `docs/00_decisiones_finales.md`

Este debería ser el archivo más importante para Codex. Su objetivo es decir: “esto es lo que se decidió después de resolver inconsistencias”.

Contenido recomendado:

```md
# 00 — Decisiones finales QuickBid

Este archivo contiene la verdad final de negocio y técnica luego de revisar inconsistencias entre documentos originales, `.md` internos y código actual.

Si hay conflicto entre este archivo y cualquier documento anterior, prevalece este archivo, salvo que se indique explícitamente lo contrario.

## Jerarquía documental

1. `docs/00_decisiones_finales.md`
2. `docs/API_CONTRATO_FINAL.md`
3. `docs/10_plan_correcciones_backend.md`
4. `docs/01_contexto_negocio.md` a `docs/09_checklist_entrega.md`
5. `README_BACKEND.md` y `quickbid/README.md`
6. `source_material/` como material histórico de consulta, no como fuente final

## Criterio general

El backend debe adaptarse razonablemente al contrato original de endpoints y pantallas, pero no de forma ciega. Si el código actual tiene una solución mínima, sólida o técnicamente mejor, puede mantenerse, siempre que quede documentado en el contrato final.

Los endpoints admin son auxiliares para pruebas y gestión manual/backoffice, sin frontend propio.

Las tablas legacy siguen siendo intocables: no se modifican columnas, constraints, enums ni relaciones legacy. Cualquier extensión se hace mediante tablas nuevas `app_*`.

Las diferencias inevitables al traducir el SQL legacy a PostgreSQL deben documentarse como normalizaciones técnicas, sin alterar la intención funcional original.

## Auth, registro y cuenta bloqueada

- Login solo con email y contraseña.
- Access token: 15 minutos.
- Refresh token: 30 días, rotativo y revocable.
- Token de recuperación de contraseña: 30 minutos y un solo uso.
- Token final de registro/setup: 48 horas y un solo uso.
- Reenvío de link de finalización de registro invalida el link anterior.
- Las respuestas de recuperación/reenvío deben ser genéricas para no revelar si un email existe.
- El campo de etapa 3 debe llamarse `setup_token`, no `token`.
- Deben existir dos formas de cambiar contraseña:
  - desde recuperación por mail;
  - desde sesión activa, sin mail.
- Una cuenta `bloqueada_permanente` puede iniciar sesión de forma limitada solo para mostrar pantalla informativa de bloqueo.
- Una cuenta `bloqueada_permanente` no puede navegar funciones reales de la app.
- El login debe devolver el estado de cuenta para que el frontend decida si muestra pantalla normal, restricción por multa o bloqueo permanente.

## DNI y archivos de registro

- DNI acepta únicamente imágenes, no PDF.
- Frente y dorso son obligatorios como dos archivos separados.
- Formatos mínimos aceptados: JPG/JPEG y PNG.
- WebP es opcional; si el backend actual no lo soporta, no es obligatorio agregarlo ahora.
- Debe existir límite configurable de tamaño para imágenes.

## Modo invitado

El invitado puede consultar subastas, catálogos e ítems, pero sin datos económicos ni estado vivo real.

El invitado nunca ve:

- precio base;
- mejor oferta actual;
- historial de pujas;
- cantidad de pujas;
- ítem actualmente en vivo;
- estado interno de lote como subastado, próximo o en puja activa.

El invitado sí puede ver:

- descripción general de subastas;
- descripción general de ítems;
- imágenes;
- datos no económicos;
- si una subasta está activa o futura, sin detalles internos del progreso.

Cualquier intento de pujar, consignar, comprar o usar funciones privadas debe bloquearse por falta de login.

## Inscripción a subastas

- La inscripción cierra 60 minutos antes del inicio de la subasta.
- No usar la regla de 30 minutos.
- La inscripción puede hacerse con medio de pago:
  - pendiente de verificación;
  - vencido;
  - verificado.
- Si el medio está pendiente o vencido, la inscripción queda pendiente de validación/revalidación manual.
- Una inscripción rechazada no bloquea un nuevo intento en la misma subasta con otro medio de pago.
- La inscripción rechazada puede conservarse como historial.
- La inscripción no crea asistencia.
- La asistencia se crea recién con la primera puja aceptada.
- Un medio validado por inscripción o por validación inicial puede usarse en cualquier subasta de la misma moneda mientras siga vigente.
- La inscripción aprobada puede generar notificaciones/email de inicio de subasta.

## Medios de pago

Tipos soportados:

- tarjeta;
- cuenta bancaria;
- cheque certificado.

Estados finales:

- `pendiente_verificacion`
- `verificado`
- `rechazado`
- `vencido`
- `eliminado`

Reglas:

- La verificación dura 5 días hábiles.
- Al vencer, el medio debe pasar a `vencido`, idealmente mediante job automático.
- Un admin puede revalidar un medio `vencido` sin que el usuario lo cargue de nuevo.
- Un admin puede revalidar un medio ya `verificado`, reiniciando el plazo de 5 días hábiles.
- Al validar/revalidar, la empresa debe cargar obligatoriamente `limiteAprobado`.
- Un medio verificado sin límite aprobado nunca debe interpretarse como infinito.
- Tarjetas y cuentas bancarias no necesitan reinicio mensual ni rolling de 30 días para MVP.
- El límite se ajusta manualmente cuando la empresa valida o revalida el medio.
- Cheques certificados consumen contra monto fijo sin reinicio mensual.
- Solo puede haber un medio principal por moneda.
- Como máximo puede haber dos principales: uno ARS y uno USD.
- Los medios no se editan, salvo marcar principal.
- La eliminación siempre es lógica.
- No se pueden eliminar medios asociados a operaciones pendientes.
- La validación suma puntos solo cuando la empresa aprueba el medio.
- Un medio recién registrado no puede usarse hasta ser validado, salvo para acciones puntuales que justamente inician validación, como inscripción a subasta.

## Puntos, reputación y categorías

Usar estos valores finales:

- Medio de pago verificado: +30
- Cada puja aceptada: +1
- Ganar una puja: +80
- Completar pago de compra a tiempo: +60
- Consignación aceptada y finalmente puesta en subasta: +70
- Pagar comisiones/envío dentro de plazo: +20
- Generar multa: -90
- Multa vencida sin pago: -250

Reglas:

- Los puntos nunca bajan de 0.
- La categoría puede subir o bajar automáticamente según puntos.
- La empresa puede ajustar puntos manualmente desde endpoint admin.
- Todo ajuste manual recalcula categoría.
- Siempre se guarda historial de movimientos de puntos.

Rangos finales:

- `comun`: 0–249
- `especial`: 250–699
- `plata`: 700–1499
- `oro`: 1500–2999
- `platino`: 3000+

## Pujas y realtime

- Primera puja puede ser igual al precio base.
- Subastas `oro` y `platino`: no aplican mínimo 1% ni máximo 20%; solo debe superar por 1 unidad a la mejor puja anterior.
- Subastas no oro/platino: mínimo = mejor puja + 1% del precio base; máximo = mejor puja + 20% del precio base.
- Un usuario no puede pujar por un producto que él mismo consignó.
- Un usuario no puede tener una puja ganadora activa en otra subasta al mismo tiempo.
- El usuario puede ver en vivo una subasta aunque no pueda pujar por falta de medio/categoría.
- Debe implementarse cierre automático del lote luego de 60 segundos sin superar la mejor puja.
- Luego de cerrar un lote, se recomienda un delay de 60 segundos antes de activar el siguiente ítem.
- Al cerrar el último ítem, se recomienda un delay de 120 segundos antes de finalizar la subasta completa.
- `idempotencyKey` debe ser obligatorio para cada puja.
- El backend debe rechazar duplicados/concurrentes con misma `idempotencyKey`.
- El frontend debe bloquear el envío de otra puja hasta recibir confirmación.
- Mientras una puja sea ganadora, debe reservar/restar capacidad del medio de pago.
- Si esa puja es superada, debe liberarse/restaurarse esa reserva.
- Si esa puja termina siendo ganadora final, la reserva pasa a consumo real.
- Las alertas frecuentes de puja superada deben priorizar WebSocket sobre notificaciones persistentes.

## Compras, multas y pagos

- Si falla el cobro inmediato, se genera compra con estado `multa_activa`.
- Multa automática: 10% del valor ofertado.
- Artículo + multa se pagan juntos en un único pago.
- Plazo para regularizar artículo + multa: 72 horas.
- Si la multa vence sin pago, la cuenta pasa a `bloqueada_permanente`.
- Pagar artículo + multa quita la restricción por multa activa si no quedan otras multas.
- Comisiones y envío se pagan en un segundo paso separado.
- Si no se pagan comisiones/envío en plazo, la empresa puede quedarse con el dinero ya pagado y con el ítem.
- Para este abandono, preferir job automático; no depender de manejo manual admin salvo endpoint auxiliar de mantenimiento.
- Modalidades de entrega: retiro personal y envío a domicilio.
- Una vez pagadas comisiones/envío, no se puede cambiar modalidad de entrega.
- La dirección de envío queda congelada al pagar extras.
- Comisión comprador se calcula sobre precio final ofertado, no sobre precio base.
- Comisión vendedor se calcula sobre precio final ofertado.
- Si compra la empresa por falta de pujas, no se cobra comisión comprador.
- Si nadie puja, la empresa compra siempre al precio base.
- La compra interna de empresa debe usar un cliente técnico/sistema.

## Documentos y PDFs

Documentos principales:

- comprobante/factura de compra;
- recibo de multa;
- acuerdo de consignación;
- liquidación de venta;
- comprobante de pago de envío de devolución.

Reglas:

- Preferir PDFs reales antes que bytes simulados.
- Comprobante/factura de compra se genera cuando la compra queda completamente pagada, incluyendo extras.
- Recibo de multa se genera solo cuando la multa se paga.
- Acuerdo de consignación se genera al aceptar el acuerdo.
- Liquidación de venta se genera cuando se paga/liquida al vendedor.
- Comprobante de envío de devolución se genera cuando se paga el envío de devolución.
- El usuario ve solo la última versión válida de cada documento.
- Se pueden conservar versiones históricas internamente si no complica demasiado.
- Los documentos pueden verse en la app y también enviarse por mail.
- Agregar opción/endpoint para descargar o reenviar documentos si es razonable.
- Los documentos no deben borrarse automáticamente.

## Consignación

- Crear consignación requiere mínimo 6 fotos.
- Máximo 15 fotos.
- Si supera 15 fotos, se rechaza.
- `segmento` significa rubro/tema del bien: arte, joyas, vehículos, relojería, etc.
- `categoria` queda reservada para común/especial/plata/oro/platino.
- Separar explícitamente `segmento` y `categoriaSubasta` en backend/docs.
- Al subir documentación de origen, la solicitud debe volver a un estado que requiera revisión manual. Se recomienda `pendiente_revision` para simplificar el flujo, dejando auditoría de que hubo documentación recibida.
- Puede existir estado explícito `rechazo_revision_fisica` antes de pasar a `devolucion_pendiente`.
- Si el usuario rechaza el acuerdo, pasa a `devolucion_pendiente`.
- Si la empresa rechaza en revisión física, pasa a `devolucion_pendiente`.
- El producto legacy se crea recién cuando el usuario acepta el acuerdo.
- Una vez creado el producto legacy por acuerdo aceptado, ya no puede rechazarse la consignación.
- Una solicitud de consignación es siempre 1 solicitud = 1 bien.
- “Colección” es solo un nombre/título comercial de subasta, no una entidad especial.
- No se requiere `duenio` legacy para iniciar consignación.
- Sí se requiere `duenio` legacy validado antes de proponer acuerdo.
- El `verificador` del dueño puede ser distinto del `revisor` del producto.

## Direcciones de envío

- Permitir hasta 5 direcciones activas por usuario.
- Solo una dirección principal.
- El usuario puede administrar direcciones desde perfil.
- El domicilio legal puede ser distinto de las direcciones de envío.
- La compra copia/congela la dirección elegida al pagar comisiones/envío.
- Las direcciones deben manejarse parecido a medios de pago: evitar edición destructiva, usar baja lógica y mantener consistencia histórica.

## Notificaciones

- Para MVP, puede mantenerse modelo simple `leida/no_leida`.
- No hace falta archivar desde la app.
- Notificaciones leídas pueden eliminarse automáticamente luego de 30 días.
- Notificaciones no leídas pueden eliminarse automáticamente luego de 90 días.
- Implementar job de limpieza si es razonable.
- Los documentos generados no se borran aunque se borre una notificación asociada.
- Generar notificación in-app cuando:
  - se aprueba/rechaza un medio de pago;
  - se aprueba/rechaza una consignación;
  - se genera una multa;
  - una subasta inscrita está por iniciar.
- Evitar persistir notificaciones demasiado frecuentes de pujas si eso genera ruido; priorizar WebSocket para eventos live.

## Estadísticas, historial y filtros

- Estadísticas soportan `mes`, `trimestre`, `anual` y `total`.
- El filtro `periodo` debe afectar realmente los cálculos.
- Separar estadísticas como comprador/postor y como vendedor/consignador.
- Historial incluye pujas ganadas, perdidas y superadas.
- Historial debe ser paginado.
- Compras pueden filtrarse por estado desde backend.
- Consignaciones pueden filtrarse por activas, rechazadas y vendidas.

## Jobs automáticos y vencimientos

Preferir jobs automáticos para:

- vencer medios verificados;
- bloquear cuentas con multas vencidas;
- marcar compras abandonadas por falta de pago de comisiones/envío;
- marcar devoluciones vencidas;
- limpiar notificaciones antiguas.

Si algún job no se implementa todavía, debe existir endpoint admin auxiliar para simular/procesar vencimientos manualmente en pruebas.

## Estados finales mínimos

Estados importantes confirmados:

- Medio pendiente: `pendiente_verificacion`
- Cuenta con multa activa: `restriccion_multa`
- Cuenta bloqueada por multa vencida: `bloqueada_permanente`

Los enums finales de cuenta, medio de pago, compra, consignación, inscripción, puja y documento deben documentarse en `docs/API_CONTRATO_FINAL.md`.

El backend puede tener estados internos detallados, pero debe exponer estados estables al frontend.
```

---

## 2.2. Crear `docs/API_CONTRATO_FINAL.md`

Este archivo debería reemplazar en la práctica a `docs/03_api_contract.md` como contrato final para frontend.

No necesariamente tenés que borrar `03_api_contract.md`; mejor dejarlo como “contrato base/original” y crear este como contrato consolidado.

Contenido recomendado:

````md
# API_CONTRATO_FINAL — QuickBid

Este archivo define el contrato final que debe consumir el frontend.

Si hay diferencias con `docs/03_api_contract.md` o con `source_material/endpoints_originales.md`, prevalece este archivo.

## Convenciones generales

Todas las respuestas siguen el formato:

```json
{
  "data": {},
  "message": "Mensaje descriptivo",
  "errors": null
}
````

o, en error:

```json
{
  "data": null,
  "message": "Mensaje descriptivo",
  "errors": [
    {
      "field": "campo",
      "code": "CODIGO_ERROR",
      "message": "Detalle"
    }
  ]
}
```

Salvo auth, registro y vistas públicas/invitado, todos los endpoints requieren `Authorization: Bearer <access_token>`.

Los endpoints admin son auxiliares y no tienen frontend formal.

## Auth y registro

### POST `/api/auth/registro/etapa1`

Crea solicitud inicial de registro.

Body:

```json
{
  "email": "usuario@email.com",
  "nombre": "Nombre",
  "apellido": "Apellido",
  "domicilioLegal": "Texto domicilio",
  "idPaisOrigen": 32
}
```

### POST `/api/auth/registro/etapa2`

Multipart.

Campos:

* `email`
* `fotoFrenteDni`
* `fotoDorsoDni`

Reglas:

* DNI solo imágenes.
* Mínimo JPG/JPEG y PNG.
* WebP opcional si está implementado.
* No aceptar PDF.
* Frente y dorso obligatorios.

### POST `/api/auth/registro/verificar-token`

Body:

```json
{
  "token": "..."
}
```

### POST `/api/auth/registro/etapa3`

Body:

```json
{
  "setup_token": "...",
  "clave": "...",
  "claveConfirmacion": "..."
}
```

Reglas:

* `setup_token` dura 48 horas.
* Un solo uso.
* Al finalizar, se crea cuenta operativa y sesión.

### POST `/api/auth/registro/reenviar-link`

Body:

```json
{
  "email": "usuario@email.com"
}
```

Reglas:

* Respuesta genérica.
* No revelar si el email existe.
* Invalida link anterior si aplica.

### POST `/api/auth/login`

Body:

```json
{
  "email": "usuario@email.com",
  "clave": "..."
}
```

Respuesta normal:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "estadoCuenta": "activa",
  "usuario": {
    "id": 1,
    "email": "usuario@email.com",
    "nombre": "Nombre",
    "apellido": "Apellido",
    "categoria": "comun",
    "puntos": 0
  }
}
```

Para cuenta `bloqueada_permanente`, el backend debe permitir login limitado y devolver estado de cuenta para que el frontend muestre pantalla de bloqueo. No debe permitir navegación real.

### POST `/api/auth/refresh`

Refresh token rotativo.

### POST `/api/auth/logout`

Revoca refresh/session.

### POST `/api/auth/recuperar-clave`

Body:

```json
{
  "email": "usuario@email.com"
}
```

Respuesta genérica.

### PUT `/api/auth/cambiar-clave`

Debe soportar dos modos:

1. Recuperación por token:

```json
{
  "token": "...",
  "claveNueva": "...",
  "claveConfirmacion": "..."
}
```

2. Cambio desde sesión activa:

```json
{
  "claveActual": "...",
  "claveNueva": "...",
  "claveConfirmacion": "..."
}
```

## Usuario

### GET `/api/usuario/perfil`

Devuelve datos personales, categoría, puntos, progreso y estado de cuenta.

### GET `/api/usuario/estadisticas?periodo=mes|trimestre|anual|total`

El filtro `periodo` debe aplicarse realmente.

Debe separar, cuando sea posible:

* estadísticas como comprador/postor;
* estadísticas como vendedor/consignador.

### GET `/api/usuario/historial?page=0&limit=20`

Incluye pujas:

* ganadas;
* perdidas;
* superadas.

Debe ser paginado.

## Direcciones de envío

### GET `/api/usuario/direcciones-envio`

Lista direcciones activas.

### POST `/api/usuario/direcciones-envio`

Crea nueva dirección.

Reglas:

* máximo 5 activas;
* puede marcarse principal;
* si se marca principal, desactiva principal anterior.

### DELETE `/api/usuario/direcciones-envio/{id}`

Baja lógica.

### PATCH `/api/usuario/direcciones-envio/{id}/principal`

Marca como principal.

Regla:

* una sola principal por usuario.

Nota: evitar edición destructiva. Si se requiere cambio fuerte, crear nueva dirección y dar de baja la anterior.

## Notificaciones

### GET `/api/usuario/notificaciones`

Filtros sugeridos:

* `categoria`
* `leidas`
* `page`

### PATCH `/api/usuario/notificaciones/{id}/leer`

Permite marcar una notificación individual como leída.

También puede aceptar `id=all` para marcar todas como leídas si no complica el backend.

Si implementar `id=all` complica mucho o empeora la claridad técnica, se permite mantener endpoint separado `/api/usuario/notificaciones/all/leer`, pero debe quedar documentado como excepción.

## Medios de pago

### GET `/api/usuario/medios-pago`

Lista medios propios.

### POST `/api/usuario/medios-pago`

Crea medio en estado `pendiente_verificacion`.

Tipos:

* `tarjeta`
* `cuenta_bancaria`
* `cheque_certificado`

Monedas:

* `ARS`
* `USD`

Reglas:

* no usable hasta verificación, salvo para iniciar inscripción/revalidación;
* no edición posterior salvo principal;
* no guardar PAN completo ni CVV.

### DELETE `/api/usuario/medios-pago/{id}`

Borrado lógico.

Bloquear si tiene operaciones pendientes.

### PATCH `/api/usuario/medios-pago/{id}/principal`

Uno por moneda.

## Subastas

### GET `/api/subastas`

Público.

Invitado:

* no ve precios;
* no ve estado vivo interno;
* no ve ítem activo.

Autenticado:

* puede ver datos ampliados según permisos.

### GET `/api/subastas/{id}`

Público con DTO distinto para invitado/autenticado.

### GET `/api/subastas/{id}/catalogo`

Público.

Invitado:

* no ve precio base;
* no ve cantidad de pujas;
* no ve mejor oferta;
* no ve estado real de lote.

### GET `/api/items/{id}`

Público con DTO reducido para invitado.

### POST `/api/subastas/{id}/inscribirse`

Body:

```json
{
  "idMedioPago": 123
}
```

Reglas:

* puede hacerse hasta 60 minutos antes del inicio;
* acepta medio `pendiente_verificacion`, `vencido` o `verificado`;
* si requiere validación/revalidación, queda pendiente;
* inscripción rechazada no impide nuevo intento con otro medio;
* inscripción no crea asistencia.

### POST `/api/subastas/{id}/verificacion`

Sin tilde en URL.

Verifica si el usuario puede participar/pujar.

### GET `/api/subastas/{id}/puja-actual`

Protegido.

Puede devolver `puedePujar=false` aunque el usuario pueda mirar.

### POST `/api/subastas/{id}/pujar`

Body:

```json
{
  "idItem": 1,
  "valorOfertado": 10000,
  "idMedioPago": 1,
  "idempotencyKey": "uuid"
}
```

Reglas:

* `idempotencyKey` obligatorio;
* primera puja puede ser precio base;
* oro/platino: superar por 1 unidad;
* resto: mínimo +1%, máximo +20%;
* usuario no puede pujar por producto propio;
* no puede tener puja ganadora activa en otra subasta;
* mientras es puja ganadora, reserva límite del medio;
* si es superada, libera reserva.

## Compras

### GET `/api/compras?estado=...`

Debe filtrar por estado si se envía.

### GET `/api/compras/{id}`

Detalle con desglose.

### PUT `/api/compras/{id}/entrega`

Define modalidad:

* `retiro`
* `envio`

Si es envío, requiere dirección.

### POST `/api/compras/{id}/pagar`

Paga comisiones/envío/extras pendientes.

Factura/comprobante de compra se genera cuando la compra queda completamente pagada.

### POST `/api/compras/{id}/pagar-con-multa`

Paga artículo + multa juntos.

Recibo de multa se genera cuando la multa se paga.

### GET `/api/compras/{id}/documentos`

Lista documentos visibles para el usuario.

## Consignaciones

### GET `/api/consignaciones/requisitos`

Valida requisitos para iniciar consignación.

### POST `/api/consignaciones`

Multipart o request mixto según implementación.

Reglas:

* mínimo 6 fotos;
* máximo 15 fotos;
* si supera 15, rechazar;
* `segmento` es rubro/tema del bien;
* no confundir `segmento` con `categoriaSubasta`.

### POST `/api/consignaciones/{id}/documentacion-origen`

Debe dejar la solicitud nuevamente pendiente de revisión manual. Estado recomendado: `pendiente_revision`.

### GET `/api/consignaciones?filtro=activas|rechazadas|vendidas`

Debe filtrar.

### GET `/api/consignaciones/{id}`

Detalle con timeline.

### POST `/api/consignaciones/{id}/acuerdo/aceptar`

Body:

```json
{
  "leyoContrato": true,
  "aceptaClausulasPlazos": true
}
```

Recomendación final: ambos deben ser `true`.

Al aceptar:

* se genera acuerdo;
* se crea producto legacy;
* no se puede rechazar posteriormente;
* se puede avanzar a publicación/subasta.

### POST `/api/consignaciones/{id}/acuerdo/rechazar`

Pasa a `devolucion_pendiente`.

### POST `/api/consignaciones/{id}/devolucion`

Elige modalidad de devolución.

### POST `/api/consignaciones/{id}/devolucion/pagar-envio`

Genera comprobante de pago de envío de devolución.

## Admin auxiliar

Sin frontend formal.

Debe permitir, como mínimo:

* aprobar/rechazar usuarios;
* validar/rechazar/revalidar medios de pago;
* cargar `limiteAprobado`;
* ajustar puntos;
* crear/editar datos de subastas demo;
* aprobar/rechazar consignaciones;
* validar `duenio`;
* proponer acuerdo;
* publicar consignación;
* cerrar lote/subasta si hace falta;
* procesar vencimientos si no hay job automático;
* reenviar/regenerar documentos si se implementa.

````

---

## 2.3. Crear `docs/10_plan_correcciones_backend.md`

Este archivo sirve para Codex como checklist directo de implementación. Mucho más útil que darle toda la historia.

Contenido recomendado:

```md
# 10 — Plan de correcciones backend post revisión

Este archivo lista las correcciones necesarias para alinear el backend actual con las decisiones finales.

Antes de modificar código, leer:

1. `AGENTS.md`
2. `docs/00_decisiones_finales.md`
3. `docs/API_CONTRATO_FINAL.md`
4. este archivo

## Prioridad alta

### Auth

- Cambiar duración access token a 15 minutos.
- Cambiar duración refresh token a 30 días.
- Permitir login limitado para cuenta `bloqueada_permanente`.
- Login limitado debe devolver estado de cuenta, pero no permitir navegación real.
- Agregar/ajustar cambio de contraseña desde sesión activa.
- Mantener recuperación por token de mail.
- En etapa 3, aceptar `setup_token` como nombre final de campo.
- Reenvío de link debe invalidar links anteriores.
- Respuestas de recuperación/reenvío deben ser genéricas.

### DNI

- Asegurar que `etapa2` use campos `fotoFrenteDni` y `fotoDorsoDni`.
- DNI no acepta PDF.
- Mantener JPG/JPEG y PNG como mínimo.
- WebP es opcional si ya está implementado o si no requiere mucho cambio.

### Inscripciones

- Mantener cierre de inscripción en 60 minutos antes del inicio.
- Permitir inscripción con medio `pendiente_verificacion`, `verificado` o `vencido`.
- Permitir nuevo intento si inscripción anterior fue rechazada.
- No crear asistencia con inscripción.
- Crear asistencia recién con primera puja aceptada.
- Inscripción aprobada puede disparar email/notificación de inicio de subasta.

### Medios de pago

- Al validar/revalidar, exigir `limiteAprobado`.
- Nunca tratar límite null como infinito.
- Permitir revalidar medio `vencido`.
- Permitir revalidar medio `verificado`, reiniciando 5 días hábiles.
- Agregar job o endpoint admin para pasar medios expirados a `vencido`.
- Mantener principal único por moneda.
- Mantener borrado lógico y bloqueo si hay operaciones pendientes.
- Ajustar puntos por validación a +30.

### Puntos

Usar valores:

- +30 medio verificado
- +1 puja aceptada
- +80 ganar puja
- +60 compra completada a tiempo
- +70 consignación puesta en subasta
- +20 pago de comisiones/envío dentro de plazo
- -90 multa generada
- -250 multa vencida

Asegurar:

- puntos nunca menores a 0;
- recálculo de categoría;
- historial de movimientos;
- admin puede ajustar puntos.

### Pujas

- Hacer `idempotencyKey` obligatorio.
- Rechazar duplicados por `idempotencyKey`.
- Frontend/backend no deben permitir segunda puja hasta confirmación.
- Implementar cierre automático de lote a los 60 segundos sin superación.
- Implementar delay recomendado de 60 segundos antes de activar siguiente ítem.
- Implementar delay recomendado de 120 segundos antes de finalizar subasta completa luego del último ítem.
- Reservar límite del medio mientras la puja sea ganadora.
- Liberar reserva si la puja es superada.
- Convertir reserva en consumo real si queda ganadora final.
- Mantener bloqueo de puja sobre producto propio.

### Compras y comisiones

- Comisión comprador sobre precio final ofertado.
- Comisión vendedor sobre precio final ofertado.
- Si compra la empresa por falta de pujas, no cobrar comisión comprador.
- Si nadie puja, empresa compra al precio base usando cliente técnico/sistema.
- Si falla cobro inmediato, compra queda `multa_activa`.
- Artículo + multa se pagan juntos.
- Comisiones/envío se pagan en segundo paso.
- Dirección queda congelada al pagar extras.
- Implementar o dejar preparado job para abandono por falta de pago de extras.

### Consignación

- Mínimo 6 fotos, máximo 15.
- Rechazar si supera 15.
- Separar `segmento` de `categoriaSubasta`.
- `segmento`: rubro/tema.
- `categoriaSubasta`: común/especial/plata/oro/platino.
- Documentación de origen debe volver a revisión manual, preferentemente `pendiente_revision`.
- Aceptar acuerdo debe validar checkboxes `leyoContrato` y `aceptaClausulasPlazos`.
- Producto legacy se crea solo al aceptar acuerdo.
- No permitir rechazo posterior a creación de producto legacy.
- `duenio` legacy no se requiere al iniciar, pero sí antes de proponer acuerdo.
- `verificador` de dueño y `revisor` de producto pueden ser distintos.

### Direcciones

- Permitir hasta 5 direcciones activas.
- Una principal.
- Administración desde perfil.
- Baja lógica.
- Evitar edición destructiva.
- Congelar dirección elegida al pagar envío/comisiones.

### Notificaciones

- Mantener modelo simple `leida/no_leida` salvo que sea muy fácil agregar archivado.
- No implementar archivado desde app.
- Limpiar leídas luego de 30 días.
- Limpiar no leídas luego de 90 días.
- No borrar documentos por borrar notificaciones.
- Generar notificaciones para:
  - medio aprobado/rechazado;
  - consignación aprobada/rechazada;
  - multa generada;
  - subasta inscrita por iniciar.
- Evitar persistir eventos muy frecuentes de pujas si generan ruido.

### Estadísticas e historial

- `GET /api/usuario/estadisticas` debe aplicar `periodo`.
- Soportar `mes`, `trimestre`, `anual`, `total`.
- Separar métricas comprador/postor y vendedor/consignador si es viable.
- Historial incluye ganadas, perdidas y superadas.
- Compras filtran por estado.
- Consignaciones filtran por activas, rechazadas y vendidas.

### Documentos

- Preferir PDFs reales.
- Generar factura/comprobante cuando compra queda completamente pagada.
- Generar recibo de multa cuando multa se paga.
- Generar acuerdo al aceptar consignación.
- Generar liquidación al liquidar vendedor.
- Generar comprobante de devolución al pagar envío de devolución.
- Usuario ve última versión válida.
- Documentos visibles en app y reenviables por mail si se implementa.

## Prioridad media

- Endpoint o job para procesar vencimientos.
- Descargar/reenviar documentos.
- Enums finales documentados y estables.
- WebSocket para eventos live, evitando persistencia excesiva.

## Prioridad baja/documental

- Documentar normalización legacy a PostgreSQL.
- Actualizar `quickbid/README.md`.
- Actualizar `quickbid/docs/CHECKLIST_FINAL.md` para que no marque como completo algo que todavía requiere ajuste.
````

---

## 2.4. Crear `source_material/respuestas_finales_inconsistencias_2026-06-06.md`

Esto no sería fuente principal para Codex, pero sí conviene guardar tus respuestas originales como “evidencia/histórico”.

Contenido:

```md
# Respuestas finales a inconsistencias — 2026-06-06

Este archivo conserva el banco de preguntas y respuestas usado para resolver contradicciones entre documentación, `.md` internos y backend actual.

No es la fuente principal de implementación. La fuente principal resumida y consolidada es:

- `docs/00_decisiones_finales.md`
- `docs/API_CONTRATO_FINAL.md`
- `docs/10_plan_correcciones_backend.md`

[pegar acá completo el texto de preguntas y respuestas]
```

---

# 3. Archivos existentes que deberías modificar

## 3.1. Modificar `AGENTS.md`

Este es clave porque Codex lo lee primero.

Cambiar la sección “Lectura obligatoria” para que quede así:

```md
## Lectura obligatoria antes de tocar código

1. `docs/00_decisiones_finales.md` — verdad final de reglas y decisiones posteriores a la revisión.
2. `docs/API_CONTRATO_FINAL.md` — contrato final de endpoints, DTOs, parámetros y estados expuestos.
3. `docs/10_plan_correcciones_backend.md` — correcciones concretas pendientes sobre el backend actual.
4. `README_BACKEND.md` — visión general, stack, cómo correr/probar y decisiones operativas.
5. `docs/01_contexto_negocio.md` — dominio funcional y reglas centrales.
6. `docs/02_reglas_negocio.md` — reglas detalladas, siempre subordinadas a `00_decisiones_finales.md`.
7. `docs/03_api_contract.md` — contrato base/original, subordinado a `API_CONTRATO_FINAL.md`.
8. `docs/04_modelo_datos.md` — tablas legacy, tablas nuevas y relaciones.
9. `docs/05_flujos_backend.md` — flujos transaccionales esperados.
10. `docs/06_realtime_websocket.md` — eventos realtime.
11. `docs/07_admin_auxiliar.md` — endpoints internos de prueba/backoffice.
12. `docs/08_validaciones_errores_seguridad.md` — validaciones, seguridad, errores, auditoría y archivos.
13. `docs/09_checklist_entrega.md` — checklist general.
14. `source_material/` — material histórico original. No prevalece sobre `00_decisiones_finales.md`.
```

Y cambiar la regla que decía “si hay conflicto con material original, usar material original” por:

```md
Si existe conflicto entre documentos:

1. Prevalece `docs/00_decisiones_finales.md`.
2. Para endpoints/DTOs/enums expuestos, prevalece `docs/API_CONTRATO_FINAL.md`.
3. Para tareas concretas de corrección, usar `docs/10_plan_correcciones_backend.md`.
4. El material de `source_material/` sirve como contexto histórico, pero no debe contradecir decisiones finales.
```

También agregaría esta regla:

```md
No marcar como completado en README/checklists algo que no haya sido alineado con `00_decisiones_finales.md` y verificado con tests o revisión manual.
```

---

## 3.2. Modificar `INDEX.md`

Agregar los nuevos archivos al índice:

```md
- `docs/00_decisiones_finales.md`: verdad final de negocio y técnica posterior a la revisión de inconsistencias.
- `docs/API_CONTRATO_FINAL.md`: contrato final que debe consumir el frontend.
- `docs/10_plan_correcciones_backend.md`: checklist concreto de correcciones pendientes sobre backend actual.
- `source_material/respuestas_finales_inconsistencias_2026-06-06.md`: respuestas originales usadas para consolidar decisiones finales.
```

Y modificar “Uso recomendado con Codex”:

```md
## Uso recomendado con Codex

1. Pedirle a Codex que lea primero `AGENTS.md`.
2. Luego pedirle que lea obligatoriamente:
   - `docs/00_decisiones_finales.md`
   - `docs/API_CONTRATO_FINAL.md`
   - `docs/10_plan_correcciones_backend.md`
3. Recién después pedir cambios de código.
4. En cada cambio, exigir que actualice documentación y tests relacionados.
```

---

## 3.3. Modificar `README_BACKEND.md`

Actualmente tiene decisiones genéricas tipo “access token 15 a 60 min” y “refresh 7 a 30 días”. Eso ya no debería quedar abierto.

Modificar “Decisiones asumidas” así:

```md
## Decisiones finales consolidadas

Las decisiones finales posteriores a la revisión de inconsistencias están en:

- `docs/00_decisiones_finales.md`
- `docs/API_CONTRATO_FINAL.md`
- `docs/10_plan_correcciones_backend.md`

Resumen operativo:

- Access token: 15 minutos.
- Refresh token: 30 días.
- Recovery token: 30 minutos.
- Setup token de registro: 48 horas.
- Setup/recovery tokens: un solo uso.
- Etapa 3 usa campo `setup_token`.
- DNI: imágenes, no PDF.
- Inscripción a subasta: hasta 60 minutos antes del inicio.
- Medios de pago: verificación por 5 días hábiles.
- Medio verificado requiere `limiteAprobado`.
- Puntos: usar valores documentados en `00_decisiones_finales.md`.
- Comisión comprador/vendedor: calcular sobre precio final ofertado.
- Empresa compra al precio base si nadie puja.
- Consignación: mínimo 6 fotos, máximo 15.
- `segmento` no es categoría; `segmento` es rubro/tema.
- Direcciones de envío: hasta 5 activas, baja lógica, una principal.
```

Y agregaría una sección:

```md
## Advertencia sobre documentación histórica

La carpeta `source_material/` conserva textos originales. Puede contener reglas anteriores o contradicciones ya resueltas. No usar como fuente final si contradice:

1. `docs/00_decisiones_finales.md`
2. `docs/API_CONTRATO_FINAL.md`
3. `docs/10_plan_correcciones_backend.md`
```

---

## 3.4. Modificar `docs/02_reglas_negocio.md`

Este archivo debería reflejar las decisiones finales de negocio.

Cambios concretos:

### Auth/cuenta

Agregar o ajustar:

```md
- `bloqueada_permanente`: permite login limitado solo para mostrar pantalla de bloqueo. No permite navegación real ni operaciones.
```

### Puntos

Reemplazar valores actuales por los finales:

```md
Acciones que suman puntos:

- Medio de pago verificado: +30
- Puja aceptada: +1
- Ganar una puja: +80
- Completar pago de compra a tiempo: +60
- Consignación aceptada y puesta en subasta: +70
- Pagar comisiones/envío dentro de plazo: +20

Acciones que restan puntos:

- Generar multa: -90
- Multa vencida sin pago: -250
```

### Inscripción

Asegurar:

```md
- La inscripción se permite hasta 60 minutos antes del inicio.
- Una inscripción rechazada no bloquea nuevo intento con otro medio.
- La inscripción no crea asistencia.
- La asistencia se crea con la primera puja aceptada.
```

### Medios

Agregar:

```md
- Verificación vigente: 5 días hábiles.
- Validar/revalidar exige `limiteAprobado`.
- Límite nulo no significa límite infinito.
- Tarjetas/cuentas no tienen reinicio mensual automático para MVP.
- Cheques consumen contra monto fijo.
```

### Pujas

Agregar:

```md
- Cierre automático de lote: 60 segundos sin superación de mejor puja.
- Delay recomendado al próximo lote: 60 segundos.
- Delay recomendado al cierre final de subasta: 120 segundos.
- Reserva de límite mientras la puja sea ganadora; liberación si es superada.
```

### Compras

Corregir:

```md
- Comisión comprador y vendedor se calculan sobre precio final ofertado.
- Si compra la empresa por falta de pujas, no se cobra comisión comprador.
```

### Consignación

Corregir/agregar:

```md
- Mínimo 6 fotos, máximo 15.
- `segmento` es rubro/tema del bien.
- `categoriaSubasta` es común/especial/plata/oro/platino.
- `duenio` legacy no se requiere al iniciar, pero sí antes de proponer acuerdo.
```

---

## 3.5. Modificar `docs/03_api_contract.md`

Tenés dos opciones.

La más limpia:

* Dejar `03_api_contract.md` como contrato base/original.
* Agregar arriba una advertencia:

```md
> Este archivo conserva el contrato base/original. Para implementación actual y frontend, usar `docs/API_CONTRATO_FINAL.md`. Si hay contradicción, prevalece `API_CONTRATO_FINAL.md`.
```

Después podés corregir solo los puntos más peligrosos:

* `fotoFrenteDni` y `fotoDorsoDni`.
* `setup_token`.
* `/verificacion` sin tilde.
* inscripción 60 minutos.
* `periodo` en estadísticas.
* `estado` en compras.
* `id=all` para notificaciones o documentar excepción.

La mejor opción para Codex es **no duplicar demasiado** y usar `API_CONTRATO_FINAL.md` como contrato real.

---

## 3.6. Modificar `docs/04_modelo_datos.md`

Agregar/ajustar modelos:

### Medios de pago

```md
`app_medios_pago` debe tener:

- `limite_aprobado` obligatorio al verificar/revalidar;
- `consumo_actual` o estructura equivalente para controlar reserva/consumo;
- `reservado_actual` o equivalente si se implementa reserva de puja ganadora activa;
- `verificado_hasta`;
- estado `vencido`;
- baja lógica.

No interpretar `limite_aprobado = null` como sin límite.
```

### Reservas de puja

Recomiendo agregar:

```md
#### `app_reservas_medio_pago` o equivalente

Tabla/estructura para representar reserva temporal de límite mientras una puja es ganadora.

Campos sugeridos:

- `id`
- `medio_pago_id`
- `puja_live_id`
- `compra_id` nullable
- `monto`
- `moneda`
- `estado`: `activa`, `liberada`, `consumida`
- `created_at`
- `released_at`
- `consumed_at`

Regla:
- cuando la puja es superada, se libera;
- cuando la puja queda ganadora final, se consume;
- debe ser transaccional.
```

### Direcciones

Cambiar de dirección única a varias:

```md
`app_direcciones_envio`:

- máximo 5 activas por cuenta;
- una principal;
- baja lógica;
- evitar edición destructiva;
- compras deben copiar snapshot de dirección al pagar extras.
```

### Segmento/categoría

```md
`segmento` y `categoriaSubasta` deben modelarse separados.

- `segmento`: rubro/tema comercial.
- `categoriaSubasta`: común/especial/plata/oro/platino.
```

### Documentos

Agregar:

```md
`app_documentos_generados` debe cubrir:

- comprobante/factura de compra;
- recibo de multa;
- acuerdo de consignación;
- liquidación de venta;
- comprobante de envío de devolución.

El usuario ve solo última versión válida.
```

---

## 3.7. Modificar `docs/05_flujos_backend.md`

Actualizar flujos clave:

### Inscripción

```md
La inscripción se permite hasta 60 minutos antes del inicio. Puede usar medio pendiente, vencido o verificado. Si fue rechazada, no bloquea nuevo intento con otro medio. No crea asistencia.
```

### Puja

Agregar reserva:

```md
Cuando una puja pasa a ser mejor oferta, se reserva temporalmente capacidad del medio. Si la puja es superada, se libera. Si queda ganadora final, se consume.
```

Agregar cierre automático:

```md
Si pasan 60 segundos sin que la mejor puja sea superada, se cierra el lote. Luego se espera 60 segundos antes de activar el siguiente ítem. Si era el último ítem, se espera 120 segundos y se finaliza la subasta.
```

### Compra

Corregir cálculo:

```md
Comisión comprador y vendedor se calculan sobre precio final ofertado.
```

### Consignación

```md
La documentación adicional/origen vuelve el flujo a revisión manual. Para simplificar, puede usarse `pendiente_revision` con auditoría de documentación recibida.
```

---

## 3.8. Modificar `docs/06_realtime_websocket.md`

Agregar:

```md
## Cierre automático y timers

- Cada lote tiene una ventana de 60 segundos desde la última mejor puja.
- Si no hay superación, el lote se cierra automáticamente.
- Luego de cerrar un lote, se recomienda esperar 60 segundos antes de activar el siguiente.
- Luego de cerrar el último lote, se recomienda esperar 120 segundos antes de finalizar la subasta.

## Eventos adicionales recomendados

- `LOTE_CIERRE_PROGRAMADO`
- `LOTE_CERRADO`
- `PROXIMO_LOTE_PROGRAMADO`
- `LOTE_ACTIVADO`
- `SUBASTA_CIERRE_PROGRAMADO`
- `SUBASTA_FINALIZADA`
- `RESERVA_MEDIO_ACTIVA`
- `RESERVA_MEDIO_LIBERADA`
- `RESERVA_MEDIO_CONSUMIDA`
```

Y aclarar:

```md
Las notificaciones frecuentes como puja superada deben priorizar WebSocket y no necesariamente persistirse como notificación in-app.
```

---

## 3.9. Modificar `docs/07_admin_auxiliar.md`

Agregar endpoints/acciones admin necesarias:

```md
## Medios de pago

- Validar medio con `limiteAprobado` obligatorio.
- Revalidar medio vencido.
- Revalidar medio verificado reiniciando 5 días hábiles.
- Procesar vencimiento manual si no hay job.

## Usuarios

- Ajustar puntos manualmente.
- Recalcular categoría.
- Desbloquear cuenta bloqueada solo manualmente si la empresa lo decide.

## Vencimientos

Endpoints auxiliares sugeridos:

- `POST /api/admin/jobs/vencer-medios-pago`
- `POST /api/admin/jobs/bloquear-multas-vencidas`
- `POST /api/admin/jobs/marcar-compras-abandonadas`
- `POST /api/admin/jobs/vencer-devoluciones`
- `POST /api/admin/jobs/limpiar-notificaciones`

## Documentos

- Reenviar documento por mail.
- Regenerar documento si aplica.
```

---

## 3.10. Modificar `docs/08_validaciones_errores_seguridad.md`

Agregar validaciones:

```md
## Validaciones nuevas/finales

### Auth

- Cuenta bloqueada permanente solo puede obtener sesión limitada.
- Sesión limitada no puede consumir endpoints protegidos normales.
- `setup_token` obligatorio en etapa 3.

### Registro

- DNI no acepta PDF.
- `fotoFrenteDni` y `fotoDorsoDni` obligatorios.

### Medios

- Verificar/revalidar requiere `limiteAprobado`.
- `limiteAprobado` debe ser > 0.
- No permitir límite nulo como infinito.
- Medio vencido puede revalidarse.
- Medio verificado puede revalidarse reiniciando plazo.

### Subastas

- Inscripción cierra 60 minutos antes.
- Inscripción rechazada no bloquea nuevo intento con otro medio.

### Pujas

- `idempotencyKey` obligatorio.
- Validar reserva de límite.
- Liberar reserva si la puja es superada.
- Consumir reserva si queda ganadora final.
- Prohibir puja por producto propio.

### Compras

- Comisión sobre precio final ofertado.
- Dirección congelada al pagar extras.
- Compra empresa sin comisión comprador.

### Consignación

- Mínimo 6 fotos.
- Máximo 15 fotos.
- Checkboxes de acuerdo deben ser verdaderos.
- `duenio` obligatorio antes de proponer acuerdo, no al iniciar consignación.
```

---

## 3.11. Modificar `docs/09_checklist_entrega.md`

Agregar checklist de consistencia final:

```md
## Consistencia post revisión

- [ ] Existe `docs/00_decisiones_finales.md`.
- [ ] Existe `docs/API_CONTRATO_FINAL.md`.
- [ ] Existe `docs/10_plan_correcciones_backend.md`.
- [ ] `AGENTS.md` prioriza decisiones finales sobre source material.
- [ ] Access token 15 min.
- [ ] Refresh token 30 días.
- [ ] Login limitado para `bloqueada_permanente`.
- [ ] Etapa 3 usa `setup_token`.
- [ ] Etapa 2 usa `fotoFrenteDni` y `fotoDorsoDni`.
- [ ] Inscripción cierra a 60 min.
- [ ] Inscripción rechazada permite reintento con otro medio.
- [ ] Medio validado exige `limiteAprobado`.
- [ ] Medio vencido puede revalidarse.
- [ ] Puntos coinciden con `00_decisiones_finales.md`.
- [ ] Cierre automático de lote 60 s.
- [ ] Delay siguiente lote 60 s.
- [ ] Delay cierre subasta 120 s.
- [ ] Reserva/liberación/consumo de límite por puja ganadora activa.
- [ ] Comisión comprador/vendedor sobre precio final.
- [ ] Empresa compra al precio base sin comisión comprador.
- [ ] Consignación mínimo 6 y máximo 15 fotos.
- [ ] `segmento` separado de `categoriaSubasta`.
- [ ] Direcciones de envío hasta 5 activas.
- [ ] Notificaciones leídas/no leídas con limpieza 30/90 días o endpoint/job equivalente.
```

---

## 3.12. Modificar `quickbid/README.md`

Este README parece describir el estado real del código actual. Por eso hay que tener cuidado: no debería decir “completo” si todavía hay cosas por corregir.

Cambios importantes:

* Cambiar access token de 30 a 15.
* Cambiar refresh si no está en 30 días.
* Donde dice verificación acredita 50 puntos, cambiar a +30.
* Donde dice inscripción hasta 60 minutos, dejarlo, porque ahora decidiste mantener 60.
* Donde dice comisión comprador usa `itemsCatalogo.comision`, corregir a “debe calcularse sobre precio final ofertado”.
* Donde dice dirección única `GET/PUT /direccion-envio`, actualizar a direcciones múltiples si se implementa; o marcar como pendiente.
* Donde dice `documentacion_recibida`, ajustar a la decisión final: preferir volver a `pendiente_revision` con auditoría de documentación recibida.
* Donde marca cosas como completas, revisar contra `10_plan_correcciones_backend.md`.

Agregar al principio:

```md
> Este README describe el backend implementado. Para reglas finales post revisión, usar `docs/00_decisiones_finales.md`, `docs/API_CONTRATO_FINAL.md` y `docs/10_plan_correcciones_backend.md`. Si este README contradice esos archivos, debe actualizarse.
```

---

## 3.13. Modificar `quickbid/docs/CHECKLIST_FINAL.md`

Este archivo actualmente parece demasiado optimista porque marca muchas cosas como completas. Después de tus decisiones, algunas pasan a “parcial” hasta corregir.

Cambiar, por ejemplo:

* Auth cuenta bloqueada: de completo a parcial si todavía bloquea login.
* Medios: parcial si no exige `limiteAprobado` o no revalida vencidos.
* Puntos: parcial si usa +50 en vez de +30.
* Subastas: parcial si no hay cierre automático real 60 s + delays.
* Compras: parcial si comisión comprador no se calcula sobre precio final.
* Documentos: parcial si solo hay metadata o PDFs simulados.
* Consignación: parcial si no valida máximo 15 fotos/checks/segmento.
* Direcciones: parcial si solo hay una dirección y no hasta 5.
* Notificaciones: parcial si no hay limpieza 30/90.

Agregar sección:

```md
## Revisión post decisiones finales

Este checklist debe revalidarse contra:

- `docs/00_decisiones_finales.md`
- `docs/API_CONTRATO_FINAL.md`
- `docs/10_plan_correcciones_backend.md`

No considerar completo un ítem que contradiga esas decisiones finales.
```

---

# 4. Texto aparte para agregar a tus `.txt` / `.docx` generales

Para tus documentos generales externos, agregaría una sección nueva al final llamada:

## “Decisiones finales posteriores a revisión de consistencia”

Podés copiar este texto:

```text
DECISIONES FINALES POSTERIORES A REVISIÓN DE CONSISTENCIA — QUICKBID

Luego de comparar la documentación general, el banco de preguntas, los endpoints originales, los .md internos del backend y el código implementado, se definieron las siguientes reglas finales para evitar contradicciones.

1. Jerarquía de documentación

La fuente final de reglas del backend será:

1) docs/00_decisiones_finales.md
2) docs/API_CONTRATO_FINAL.md
3) docs/10_plan_correcciones_backend.md
4) docs/01_contexto_negocio.md a docs/09_checklist_entrega.md
5) README_BACKEND.md y quickbid/README.md
6) source_material como material histórico de consulta

Si algún texto original contradice las decisiones finales, prevalecen los documentos finales nuevos.

2. Auth y sesión

El access token dura 15 minutos.
El refresh token dura 30 días.
El token de recuperación dura 30 minutos.
El token final de registro/setup dura 48 horas.
Los tokens de setup y recuperación son de un solo uso.
El campo final para completar registro será setup_token.
El reenvío de link invalida links anteriores.
Las respuestas de recuperación/reenvío deben ser genéricas para no revelar existencia de emails.
Debe existir cambio de contraseña tanto por recuperación de mail como desde sesión activa.

Una cuenta bloqueada_permanente puede iniciar sesión de forma limitada solo para ver una pantalla de bloqueo, pero no puede navegar ni operar en la app.

3. Registro y DNI

El DNI solo acepta imágenes.
No se acepta PDF para DNI.
Frente y dorso son obligatorios.
Los formatos mínimos aceptados son JPG/JPEG y PNG.
WebP puede mantenerse como opcional si ya está implementado.
El tamaño máximo debe ser configurable.

4. Invitado

El invitado puede ver subastas, catálogos e ítems, pero sin precios ni datos vivos.
Nunca debe ver precio base, mejor oferta, historial de pujas, cantidad de pujas ni ítem actualmente en vivo.
Sí puede ver descripción, imágenes y datos generales no económicos.

5. Inscripción a subastas

La inscripción cierra 60 minutos antes del inicio.
Puede realizarse con medio pendiente, vencido o verificado.
Si el medio requiere validación o revalidación, queda pendiente de revisión manual.
Una inscripción rechazada no bloquea un nuevo intento con otro medio.
La inscripción no crea asistencia.
La asistencia se crea recién con la primera puja aceptada.
Un medio validado puede usarse en cualquier subasta de la misma moneda mientras siga vigente.

6. Medios de pago

La verificación dura 5 días hábiles.
Al vencer, el medio pasa a vencido.
Un medio vencido puede revalidarse sin ser cargado nuevamente.
Un medio verificado también puede revalidarse, reiniciando el plazo de 5 días hábiles.
Al validar o revalidar, la empresa debe cargar limiteAprobado.
Un medio sin límite aprobado no se interpreta como infinito.
Los medios no se editan, salvo marcar principal.
La eliminación es lógica.
Solo puede haber un medio principal por moneda: máximo uno ARS y uno USD.
El cheque consume contra monto fijo sin reinicio mensual.
Para simplificar, tarjetas y cuentas no tendrán reinicio mensual/rolling automático en MVP; el límite se ajusta al validar/revalidar.

7. Puntos y categorías

Valores finales:
- Medio verificado: +30
- Puja aceptada: +1
- Ganar puja: +80
- Compra pagada a tiempo: +60
- Consignación aceptada y puesta en subasta: +70
- Comisiones/envío pagados dentro de plazo: +20
- Multa generada: -90
- Multa vencida sin pago: -250

Los puntos nunca bajan de 0.
La categoría sube o baja automáticamente según puntos.
La empresa puede ajustar puntos por endpoint admin.
Todo ajuste recalcula categoría.
Se guarda historial de movimientos.

Rangos:
- comun: 0–249
- especial: 250–699
- plata: 700–1499
- oro: 1500–2999
- platino: 3000+

8. Pujas

Primera puja puede ser igual al precio base.
En oro/platino no aplican mínimo 1% ni máximo 20%; solo debe superar por 1 unidad.
En el resto, mínimo = mejor puja + 1% del precio base y máximo = mejor puja + 20% del precio base.
Un usuario no puede pujar por producto propio.
Un usuario no puede tener una puja ganadora activa en otra subasta.
Debe existir idempotencyKey obligatorio.
Debe implementarse cierre automático de lote luego de 60 segundos sin superar la mejor puja.
Luego de cerrar un lote se recomienda delay de 60 segundos antes del siguiente.
Luego del último lote se recomienda delay de 120 segundos antes de finalizar la subasta.
Mientras una puja es ganadora, reserva límite del medio.
Si es superada, libera la reserva.
Si queda ganadora final, la reserva se consume.

9. Compras, multas y comisiones

Si falla cobro inmediato, se genera compra con multa_activa.
La multa es 10% del valor ofertado.
Artículo y multa se pagan juntos.
Plazo de regularización: 72 horas.
Si vence sin pago, cuenta bloqueada_permanente.
Comisiones y envío se pagan en segundo paso.
La comisión comprador se calcula sobre precio final ofertado.
La comisión vendedor se calcula sobre precio final ofertado.
Si compra la empresa por falta de pujas, no se cobra comisión comprador.
Si nadie puja, la empresa compra al precio base.
Debe usarse cliente técnico/sistema para compras internas de empresa.

10. Documentos

Documentos principales:
- comprobante/factura de compra
- recibo de multa
- acuerdo de consignación
- liquidación de venta
- comprobante de pago de envío de devolución

Preferir PDFs reales.
El usuario ve la última versión válida.
Los documentos pueden verse en app y enviarse por mail.
No deben borrarse automáticamente.

11. Consignación

Mínimo 6 fotos.
Máximo 15 fotos.
Si supera máximo, se rechaza.
Segmento significa rubro/tema del bien.
Categoria/categoriaSubasta significa común/especial/plata/oro/platino.
El producto legacy se crea recién cuando el usuario acepta el acuerdo.
No se puede rechazar luego de crear producto legacy.
No se requiere duenio legacy para iniciar consignación.
Sí se requiere duenio legacy validado antes de proponer acuerdo.
El verificador del dueño puede ser distinto del revisor del producto.
Una solicitud de consignación es siempre 1 bien.
Colección es solo nombre/título comercial de subasta, no entidad especial.

12. Direcciones de envío

Permitir hasta 5 direcciones activas.
Una principal.
El domicilio legal puede ser distinto.
Se administran desde perfil.
Usar baja lógica y evitar edición destructiva.
La compra congela la dirección elegida al pagar comisiones/envío.

13. Notificaciones

Puede mantenerse modelo simple leida/no_leida.
No hace falta archivar desde la app.
Leídas se eliminan luego de 30 días.
No leídas se eliminan luego de 90 días.
Los documentos no se borran por borrar notificaciones.
Notificar in-app aprobación/rechazo de medio, aprobación/rechazo de consignación, multa generada y subasta inscrita por iniciar.
Eventos frecuentes de puja deben priorizar WebSocket antes que notificación persistente.

14. Estadísticas y filtros

Estadísticas soportan mes, trimestre, anual y total.
El filtro periodo debe afectar cálculos.
Separar métricas comprador/postor y vendedor/consignador si es viable.
Historial incluye pujas ganadas, perdidas y superadas.
Compras filtran por estado.
Consignaciones filtran por activas, rechazadas y vendidas.

15. Jobs y vencimientos

Preferir jobs automáticos para:
- vencer medios verificados
- bloquear cuentas con multas vencidas
- marcar compras abandonadas
- marcar devoluciones vencidas
- limpiar notificaciones

Si no se implementa job, debe existir endpoint admin auxiliar para procesar/simular vencimientos.
```

---

# 5. Qué NO tocaría

No tocaría demasiado `source_material/*`, salvo agregar el nuevo archivo con tus respuestas finales. Esos archivos son material histórico. Si los modificás, perdés trazabilidad de qué era original y qué fue decidido después.

Tampoco borraría `docs/03_api_contract.md`; lo mantendría como contrato base/original, pero claramente subordinado a `API_CONTRATO_FINAL.md`.

---

# 6. Orden ideal para pasárselo a Codex

Primero documentación, después código.

## Prompt 1 para Codex: documentación

```text
Leé AGENTS.md y luego actualizá la documentación del proyecto para incorporar las decisiones finales post revisión.

Objetivo:
- Crear docs/00_decisiones_finales.md
- Crear docs/API_CONTRATO_FINAL.md
- Crear docs/10_plan_correcciones_backend.md
- Crear source_material/respuestas_finales_inconsistencias_2026-06-06.md
- Actualizar AGENTS.md, INDEX.md, README_BACKEND.md, docs/02_reglas_negocio.md, docs/03_api_contract.md, docs/04_modelo_datos.md, docs/05_flujos_backend.md, docs/06_realtime_websocket.md, docs/07_admin_auxiliar.md, docs/08_validaciones_errores_seguridad.md, docs/09_checklist_entrega.md, quickbid/README.md y quickbid/docs/CHECKLIST_FINAL.md para que no contradigan las decisiones finales.

Regla central:
Si hay conflicto, prevalece:
1. docs/00_decisiones_finales.md
2. docs/API_CONTRATO_FINAL.md
3. docs/10_plan_correcciones_backend.md

No modifiques código todavía. Solo documentación.

Al terminar, listá qué archivos cambiaste y qué contradicciones resolviste.
```

## Prompt 2 para Codex: correcciones backend

```text
Ahora leé:
- docs/00_decisiones_finales.md
- docs/API_CONTRATO_FINAL.md
- docs/10_plan_correcciones_backend.md

Aplicá las correcciones de prioridad alta en el backend, manteniendo cambios mínimos y seguros.

En particular:
- access token 15 min
- refresh token 30 días
- login limitado para bloqueada_permanente
- cambio de contraseña desde sesión activa
- setup_token en etapa 3
- fotoFrenteDni/fotoDorsoDni en etapa 2
- inscripción 60 minutos y reintento si fue rechazada
- revalidación de medios vencidos/verificados con limiteAprobado obligatorio
- puntos finales
- idempotencyKey obligatorio
- reserva/liberación/consumo de límite mientras puja es ganadora
- comisión comprador/vendedor sobre precio final
- empresa compra al precio base sin comisión comprador
- consignación mínimo 6 máximo 15 fotos
- segmento separado de categoriaSubasta
- acuerdo con checkboxes obligatorios
- direcciones hasta 5 activas con baja lógica si todavía no está
- filtros periodo/estado/filtro en estadísticas, compras y consignaciones

Agregá o ajustá tests de integración/unitarios para cada regla crítica modificada.

No cambies tablas legacy. Usá migraciones nuevas para cualquier cambio de esquema app_.
```

## Prompt 3 para Codex: jobs/documentos/notificaciones

```text
Ahora implementá o dejá preparados los aspectos de prioridad media según docs/10_plan_correcciones_backend.md:

- jobs automáticos o endpoints admin equivalentes para vencimientos
- limpieza de notificaciones leídas/no leídas
- documentos principales: factura compra, recibo multa, acuerdo consignación, liquidación, comprobante devolución
- descarga/reenviar documento si no complica demasiado
- actualización de README y CHECKLIST_FINAL según lo realmente implementado

No marques como completo nada que no esté implementado y probado.
```

---

# 7. Recomendación final

Yo haría primero **solo la actualización documental**. Es importante porque ahora Codex, si lee los `.md` viejos, puede seguir creyendo cosas que tus respuestas ya cambiaron: por ejemplo, que el access token puede durar 30 minutos, que los puntos por medio son +50, que la comisión comprador sale de `itemsCatalogo.comision`, que la dirección es única, o que `documentacion_recibida` es el estado final correcto. Tus respuestas finales corrigen o matizan todo eso.   

Después de esa limpieza, sí conviene pedirle a Codex cambios de backend. Así trabaja contra una única verdad y no contra documentos que se contradicen.
