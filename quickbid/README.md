# QuickBid Backend

## Auth Y Registro

Se implementaron `POST /api/auth/registro/etapa1`, `etapa2`,
`verificar-token`, `etapa3`, `reenviar-link`, `login`, `refresh`, `logout`,
`recuperar-clave`, `PUT /api/auth/cambiar-clave` y `GET /api/auth/sesion`.
Los endpoints protegidos reciben `Authorization: Bearer <accessToken>`.

El catalogo de paises es publico para que registro pueda mostrar un selector
buscable sin hardcodear IDs legacy:

```text
GET /api/catalogos/paises?q=&buscar=&page=0&size=50
GET /api/catalogos/paises/{id}
```

`q` y `buscar` son alias. La busqueda ignora mayusculas/minusculas y considera
nombre, nombre corto, nacionalidad y capital. El listado se ordena por nombre;
la respuesta usa `id` como alias de `paises.numero` y mantiene el envelope
uniforme `ApiResponse`. Por ejemplo, buscar `arg` devuelve Argentina con
`id=32`, valor que debe enviarse como `idPaisOrigen` en registro etapa 1.

Login demo:

```powershell
curl.exe -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"aprobado@quickbid.demo\",\"clave\":\"Demo123!\"}'
```

El access token JWT HS256 vence por defecto en 15 minutos. Los refresh tokens
vencen en 30 dias, se guardan hasheados y rotan al usarse. Los tokens de setup
y recuperacion tambien se persisten hasheados; duran 48 horas y 30 minutos.
En desarrollo local y test la entrega de email queda simulada por defecto: el
log registra solo metadata de entrega y nunca imprime el token de un solo uso.
Con `APP_MAIL_ENABLED=true` se usa SMTP real.

Variables configurables: `APP_JWT_SECRET`, `APP_JWT_ACCESS_TOKEN_MINUTES`,
`APP_JWT_REFRESH_TOKEN_DAYS`, `APP_FILES_STORAGE_PATH`,
`APP_MAX_IMAGE_SIZE_BYTES`, `APP_MAX_FILE_SIZE`, `APP_MAX_REQUEST_SIZE` y
`APP_MAX_DOCUMENT_SIZE_BYTES`, `APP_PURCHASE_SHIPPING_FLAT_COST`,
`APP_PURCHASE_COMPLETED_POINTS`, `APP_PURCHASE_FINE_GENERATED_POINTS_PENALTY`,
`APP_PURCHASE_FINE_EXPIRED_POINTS_PENALTY`, `APP_CONSIGNMENT_RETURN_SHIPPING_FLAT_COST`,
`APP_CONSIGNMENT_PUBLISHED_POINTS`,
`APP_ADMIN_ENABLED`, `APP_ADMIN_INTERNAL_KEY`, `APP_ADMIN_EMPLOYEE_ID`,
`APP_MAIL_ENABLED`, `APP_MAIL_FROM`, `APP_MAIL_NOTIFICATIONS_ENABLED`,
`APP_FRONTEND_BASE_URL` y las variables `SPRING_MAIL_*`.

Login, recuperacion de clave y reenvio de enlace aplican rate limiting en
memoria por instancia. Antes de desplegar multiples instancias debe migrarse
ese contador a un backend compartido, por ejemplo Redis.

Ejemplo de refresh y logout:

```powershell
$login = Invoke-RestMethod -Method Post http://localhost:8080/api/auth/login `
  -ContentType 'application/json' `
  -Body '{"email":"aprobado@quickbid.demo","clave":"Demo123!"}'
$accessToken = $login.data.accessToken
$refreshToken = $login.data.refreshToken
$headers = @{ Authorization = "Bearer $accessToken" }

$refresh = @{ refreshToken = $refreshToken } | ConvertTo-Json
Invoke-RestMethod -Method Post http://localhost:8080/api/auth/refresh `
  -ContentType 'application/json' -Body $refresh
Invoke-RestMethod -Method Post http://localhost:8080/api/auth/logout `
  -ContentType 'application/json' -Body $refresh
```

El login devuelve `estadoCuenta` al nivel principal de `data`. Una cuenta
`bloqueada_permanente` puede iniciar sesion de forma limitada para que la app
muestre la pantalla informativa de bloqueo, pero ese token no habilita
navegacion ni operaciones reales. Los endpoints protegidos responden envelope
JSON uniforme: token ausente, invalido o expirado devuelve `401 UNAUTHORIZED`;
un token valido asociado a una cuenta bloqueada o deshabilitada devuelve
`403 ACCOUNT_BLOCKED`; y un usuario autenticado sin permisos suficientes
devuelve `403 FORBIDDEN`.

## Email SMTP

`MailService` selecciona implementacion por configuracion:

- `APP_MAIL_ENABLED=false`: usa el adaptador simulado para desarrollo y tests.
- `APP_MAIL_ENABLED=true`: usa SMTP real mediante Spring Mail.
- `APP_MAIL_NOTIFICATIONS_ENABLED=false`: conserva emails de auth obligatorios,
  pero apaga emails de eventos de negocio. Las notificaciones internas siguen
  persistiendo.

Configuracion base:

```powershell
$env:APP_MAIL_ENABLED='true'
$env:APP_MAIL_FROM='no-reply@quickbid.example'
$env:APP_FRONTEND_BASE_URL='https://app.quickbid.example'
$env:SPRING_MAIL_HOST='smtp.example.com'
$env:SPRING_MAIL_PORT='587'
$env:SPRING_MAIL_USERNAME='usuario-smtp'
$env:SPRING_MAIL_PASSWORD='secreto-en-variable-de-entorno'
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH='true'
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE='true'
```

Proveedores habituales:

| Proveedor | Host | Puerto | Nota |
| --- | --- | --- | --- |
| Gmail | `smtp.gmail.com` | `587` | Usar app password, no la clave normal |
| Outlook / Microsoft 365 | `smtp.office365.com` | `587` | Usar credencial SMTP habilitada |
| Mailtrap Email Sandbox | Host asignado por Mailtrap | Puerto asignado | Util para prueba manual sin entregar a destinatarios reales |

Los links de setup y recuperacion se arman con `APP_FRONTEND_BASE_URL`. El token
plano vive solo durante el armado del email: DB y auditoria conservan hashes y
los logs no imprimen tokens.

Emails obligatorios: aprobacion de registro, reenvio de setup y recuperacion de
clave. Si SMTP falla, la operacion no deja un token nuevo persistido; aprobacion
admin responde error controlado y recuperacion/reenvio mantienen su respuesta
generica para no revelar si el email existe.

Eventos de negocio enviados por email despues del commit: medio de pago
verificado/rechazado; multa generada/pagada/vencida; lote ganado; cobro de
adjudicacion exitoso; entrega o retiro pendiente; acuerdo de consignacion
disponible; documentacion adicional requerida; consignacion publicada;
liquidacion disponible; y apertura de una subasta con inscripcion vigente. Si
SMTP falla, la notificacion interna ya persistida permanece disponible y el
error se registra sin secretos. El resto de eventos queda solo como
notificacion interna para evitar ruido.

## Usuario Y Perfil

Los endpoints de usuario requieren `Authorization: Bearer <accessToken>`:

```text
GET   /api/usuario/perfil
GET   /api/usuario/estadisticas?periodo=mes|trimestre|anual|total
GET   /api/usuario/historial?page=0&size=20
GET   /api/usuario/notificaciones?categoria=&tipo=&leida=false&page=0&size=20
PATCH /api/usuario/notificaciones/{id}/leer
GET   /api/usuario/direccion-envio
PUT   /api/usuario/direccion-envio
GET   /api/usuario/direcciones-envio
POST  /api/usuario/direcciones-envio
DELETE /api/usuario/direcciones-envio/{id}
PATCH /api/usuario/direcciones-envio/{id}/principal
```

Ejemplos PowerShell, luego de guardar el token retornado por login:

```powershell
$headers = @{ Authorization = "Bearer $accessToken" }
Invoke-RestMethod http://localhost:8080/api/usuario/perfil -Headers $headers
Invoke-RestMethod 'http://localhost:8080/api/usuario/estadisticas?periodo=trimestre' -Headers $headers
Invoke-RestMethod 'http://localhost:8080/api/usuario/historial?page=0&size=10' -Headers $headers
Invoke-RestMethod 'http://localhost:8080/api/usuario/notificaciones?leida=false' -Headers $headers
Invoke-RestMethod -Method Patch http://localhost:8080/api/usuario/notificaciones/18001/leer -Headers $headers
Invoke-RestMethod http://localhost:8080/api/usuario/direccion-envio -Headers $headers
```

Para crear o actualizar la direccion principal legacy:

```powershell
$direccion = @{
  alias = 'Casa'
  destinatario = 'Ana Aprobada'
  calle = 'Av. Demo'
  numero = '100'
  codigoPostal = 'C1000'
  localidad = 'Buenos Aires'
  provincia = 'Buenos Aires'
  pais = 'Argentina'
} | ConvertTo-Json
Invoke-RestMethod -Method Put http://localhost:8080/api/usuario/direccion-envio `
  -Headers $headers -ContentType 'application/json' -Body $direccion
```

Los endpoints finales de coleccion permiten hasta cinco direcciones activas por
cuenta, usan baja logica y garantizan una unica principal:

```powershell
Invoke-RestMethod http://localhost:8080/api/usuario/direcciones-envio -Headers $headers
Invoke-RestMethod -Method Post http://localhost:8080/api/usuario/direcciones-envio `
  -Headers $headers -ContentType 'application/json' -Body $direccion
Invoke-RestMethod -Method Patch http://localhost:8080/api/usuario/direcciones-envio/5101/principal `
  -Headers $headers
Invoke-RestMethod -Method Delete http://localhost:8080/api/usuario/direcciones-envio/5101 `
  -Headers $headers
```

Una cuenta `restriccion_multa` puede consultar perfil, estadisticas, historial,
notificaciones y direccion. La restriccion debe bloquear su inscripcion y sus
pujas en los modulos de subastas.

`/api/usuario/estadisticas` acepta `mes`, `trimestre`, `anual` y `total`; el
periodo filtra los calculos y la respuesta conserva metricas agregadas
compatibles, ademas de secciones separadas `compradorPostor` y
`vendedorConsignador`. El historial es paginado e incluye pujas aceptadas,
ganadoras y superadas junto con compras.

Las notificaciones son `leida/no_leida`. La app puede marcar una o todas como
leidas; la limpieza automatizable vive en admin auxiliar y elimina leidas con
mas de 30 dias y no leidas con mas de 90 dias, sin borrar documentos.

## Medios De Pago De Usuario

Las cuentas `activa` y `restriccion_multa` pueden administrar sus propios
medios de pago con token Bearer:

```text
GET    /api/usuario/medios-pago
POST   /api/usuario/medios-pago
DELETE /api/usuario/medios-pago/{id}
PATCH  /api/usuario/medios-pago/{id}/principal
```

Una tarjeta o cuenta bancaria se registra con JSON. Todo medio nuevo queda en
`pendiente_verificacion`; no suma puntos ni puede marcarse como principal hasta
que un empleado lo verifique manualmente. La verificacion acredita 30 puntos,
genera auditoria y notificacion, y tiene vigencia de 5 dias habiles. Verificar
o revalidar exige `limiteAprobado`; un limite nulo no se interpreta como
infinito. El admin puede revalidar un medio `vencido` o `verificado` desde
`/api/admin/medios-pago/{id}/verificar`, reiniciando el plazo.

Ejemplo de tarjeta con datos ficticios:

```powershell
$tarjeta = @{
  tipo = 'tarjeta'
  moneda = 'ARS'
  nacional = $true
  titular = 'Ana Aprobada'
  numeroTarjeta = '4111111111111111'
  cvv = '123'
  vencimientoMes = 12
  vencimientoAnio = 2030
  marca = 'Visa'
} | ConvertTo-Json
Invoke-RestMethod -Method Post http://localhost:8080/api/usuario/medios-pago `
  -Headers $headers -ContentType 'application/json' -Body $tarjeta
```

Ejemplo de cuenta bancaria:

```powershell
$cuenta = @{
  tipo = 'cuenta_bancaria'
  moneda = 'USD'
  nacional = $false
  titular = 'Ana Aprobada'
  nombreBanco = 'Banco Demo'
  numeroCuenta = '000123456789'
  cbuCvu = '0000000000000000000000'
  alias = 'ana.demo.usd'
} | ConvertTo-Json
Invoke-RestMethod -Method Post http://localhost:8080/api/usuario/medios-pago `
  -Headers $headers -ContentType 'application/json' -Body $cuenta
```

Los cheques certificados usan `multipart/form-data` y exigen frente y dorso en
JPEG, PNG o WebP real:

```powershell
curl.exe -X POST http://localhost:8080/api/usuario/medios-pago `
  -H "Authorization: Bearer $accessToken" `
  -F "moneda=ARS" -F "nacional=true" -F "titular=Ana Aprobada" `
  -F "numeroCheque=CHK-DEMO-001" -F "monto=150000" `
  -F "fechaVencimiento=2030-12-31" -F "bancoEmisor=Banco Demo" `
  -F "fotoAnverso=@C:\temp\cheque-frente.png;type=image/png" `
  -F "fotoReverso=@C:\temp\cheque-dorso.png;type=image/png"
```

El backend no persiste PAN completo ni CVV. Conserva hash y ultimos cuatro
digitos de tarjeta; para cuentas bancarias conserva hashes de los
identificadores sensibles. El borrado es logico y se rechaza si el medio esta
asociado a una operacion critica. Solo puede existir un principal por moneda.
Los medios verificados expirados pueden marcarse `vencido` desde el endpoint
admin auxiliar `/api/admin/medios-pago/vencer-expirados`.

## Subastas, Catalogos E Inscripcion Previa

Las consultas generales admiten modo invitado:

```text
GET  /api/subastas
GET  /api/subastas/{id}
GET  /api/subastas/{id}/catalogo
GET  /api/items/{id}
```

El invitado ve datos descriptivos, pero no precios, comisiones, lote activo,
mejor oferta ni estado vivo. Cuando una subasta esta `en_vivo`, la vista publica
la presenta como `abierta`. Un usuario autenticado aprobado puede ver precios
base y comisiones del catalogo.

```powershell
Invoke-RestMethod http://localhost:8080/api/subastas
Invoke-RestMethod http://localhost:8080/api/subastas/6001/catalogo
Invoke-RestMethod http://localhost:8080/api/subastas/6001/catalogo -Headers $headers
Invoke-RestMethod http://localhost:8080/api/items/9001 -Headers $headers
```

Las operaciones protegidas son:

```text
POST /api/subastas/{id}/inscribirse
POST /api/subastas/{id}/verificacion
GET  /api/subastas/{id}/puja-actual
POST /api/subastas/{id}/pujar
```

La inscripcion previa manifiesta interes y asocia un medio de pago. No equivale
a participacion efectiva, no crea `asistentes` legacy y no falla porque el
usuario participe en otra subasta. Solo se permite hasta 60 minutos antes del
inicio. Alcanza un medio guardado de la misma moneda en estado
`pendiente_verificacion`, `verificado` o `vencido`; si requiere validacion o
revalidacion, queda pendiente. Una inscripcion `rechazada` se conserva como
historial y no bloquea un nuevo intento con otro medio.

En este README, una verificacion `vencida` o `expirada` significa que termino
la vigencia temporal de la validacion manual de empresa expresada por
`verificado_hasta`. No es el vencimiento propio de una tarjeta, que se valida
por separado al registrar el instrumento.

```powershell
$inscripcion = @{ medioPagoId = 5002 } | ConvertTo-Json
Invoke-RestMethod -Method Post http://localhost:8080/api/subastas/6002/inscribirse `
  -Headers $headers -ContentType 'application/json' -Body $inscripcion
Invoke-RestMethod -Method Post http://localhost:8080/api/subastas/6002/verificacion `
  -Headers $headers
```

Para pujar se aplican reglas mas estrictas: cuenta `activa`, categoria
suficiente, subasta `en_vivo`, lote activo, medio `verificado` vigente de la
misma moneda y ausencia de participacion efectiva en otra subasta. La
inscripcion previa no es obligatoria para pujar: manifiesta interes, permite
notificaciones anticipadas y puede disparar revalidacion del medio. La
asistencia legacy se crea recien con la primera puja aceptada.

```powershell
Invoke-RestMethod http://localhost:8080/api/subastas/6001/puja-actual -Headers $headers

$puja = @{
  itemCatalogoId = 9001
  monto = 25300
  medioPagoId = 5001
  clientStateVersion = 1
  idempotencyKey = 'manual-demo-6001-2'
} | ConvertTo-Json
Invoke-RestMethod -Method Post http://localhost:8080/api/subastas/6001/pujar `
  -Headers $headers -ContentType 'application/json' -Body $puja
```

`clientStateVersion` debe coincidir con el snapshot consultado. Si otra puja
llego antes, el backend responde `409 BID_OUTDATED_STATE` y el cliente debe
refrescar el estado. `idempotencyKey` es obligatorio: al repetir exactamente la
misma solicitud devuelve la puja existente sin duplicar filas, reservas ni
puntos; reutilizar la clave con otro payload devuelve `409 IDEMPOTENCY_CONFLICT`.

La aceptacion toma lock pesimista sobre `app_subasta_estado_vivo`, revalida las
reglas dentro de la transaccion, incrementa la version, deja la oferta anterior
como `superada`, inserta `app_pujas_live` y sincroniza `asistentes` y `pujos`
legacy. Para categorias `comun`, `especial` y `plata`, el incremento minimo es
1% del precio base y el maximo es 20%. Para `oro` y `platino`, alcanza una
unidad monetaria sobre la mejor oferta y no aplica ese maximo.

Cada puja aceptada suma 1 punto y crea una reserva activa sobre el limite del
medio de pago. Si la puja es superada, la reserva se libera. Si gana el lote, la
reserva se convierte en consumo real y el ganador recibe 80 puntos adicionales.
La disponibilidad de un medio considera consumo real mas reservas activas; un
limite nulo nunca se interpreta como infinito.

## Realtime De Subastas

El endpoint STOMP es `/ws`. El frame `CONNECT` debe incluir
`Authorization: Bearer <accessToken>`. Los invitados no pueden conectarse ni
suscribirse a topics live con importes.

El handshake WebSocket abre el transporte sin exigir JWT HTTP. En navegador o
React Native con `@stomp/stompjs`, enviar el token mediante
`connectHeaders.Authorization`; esos headers pertenecen al frame STOMP
`CONNECT`, no al handshake HTTP:

```javascript
const client = new Client({
  brokerURL: "ws://localhost:8080/ws",
  connectHeaders: { Authorization: `Bearer ${accessToken}` }
});
```

```text
/topic/subastas/{id}/estado
/topic/subastas/{id}/items/{itemCatalogoId}/pujas
/user/queue/notificaciones
/user/queue/pujas
```

Un header HTTP `Authorization` valido sigue siendo aceptado durante el
handshake para herramientas que lo soportan, pero no reemplaza el Bearer del
frame STOMP `CONNECT`. Cada `SUBSCRIBE` vuelve a validar que la
cuenta exista y pueda navegar. Los topics de subasta validan ademas que exista
la subasta y, para `/items/{itemCatalogoId}/pujas`, que el item pertenezca a
ella. Solo se aceptan los destinos documentados: una cola privada ajena o un
topic arbitrario se rechazan. Antes de entregar eventos live o privados se
revalida el estado actual de la cuenta para cortar tambien una sesion que haya
sido bloqueada despues de suscribirse.

La visualizacion live no exige inscripcion previa, categoria suficiente ni
medio verificado vigente. Una cuenta `restriccion_multa` tambien puede mirar.
Estas condiciones si afectan `puedePujar`, disponible en
`POST /api/subastas/{id}/verificacion` y en el snapshot
`GET /api/subastas/{id}/puja-actual`. La validacion definitiva de una puja
sigue ocurriendo transaccionalmente en `POST /api/subastas/{id}/pujar`.

El topic del item publica `MEJOR_OFERTA_ACTUALIZADA` con alias como
`Postor #15`, sin nombres ni emails. `PUJA_ACEPTADA`, `PUJA_SUPERADA` y
`PUJA_RECHAZADA` se envian por colas privadas al usuario correspondiente.
`ESTADO_ACTUALIZADO` y `LOTE_CERRADO` se publican en el topic de estado.

El broker negocia heartbeat cada 10 segundos. La presencia vive en memoria por
instancia, se elimina en `DISCONNECT` y tambien por TTL para cubrir cortes sin
frame final. Se configura con `APP_WEBSOCKET_PRESENCE_TTL_MS` y
`APP_WEBSOCKET_PRESENCE_CLEANUP_MS`; no se persiste en tablas legacy.

## Compras, Pagos Y Entrega

Los endpoints de compras requieren token Bearer. Cada usuario solo puede ver y
operar sus propias compras:

```text
GET  /api/compras?page=0&size=20&estado=pagos_extra_pendientes
GET  /api/compras/{id}
PUT  /api/compras/{id}/entrega
POST /api/compras/{id}/pagar
POST /api/compras/{id}/pagar-con-multa
GET  /api/compras/{id}/documentos
```

Ejemplos de consulta:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/compras?page=0&size=20&estado=pagos_extra_pendientes' -Headers $headers
Invoke-RestMethod http://localhost:8080/api/compras/13002 -Headers $headers
Invoke-RestMethod http://localhost:8080/api/compras/13002/documentos -Headers $headers
```

Al cerrar un lote, el backend toma lock pesimista sobre el snapshot vivo. Si
existe una oferta aceptada, la marca `ganadora`, actualiza `pujos.ganador`,
registra la compra y sincroniza `registroDeSubasta` legacy cuando existe duenio.
Luego intenta cobrar automaticamente solo el monto adjudicado. Las comisiones de
comprador y vendedor se calculan sobre el precio final adjudicado usando la
proporcion de `itemsCatalogo.comision` sobre `precioBase`, y quedan congeladas
en `app_compras`. Si nadie pujo, crea una compra interna con
`comprador_empresa=true`, precio base y comision de comprador en cero; este caso
vive en `app_compras` porque legacy exige un cliente normal.

El cobro automatico aprobado deja la compra en `pagos_extra_pendientes`. Si
falla, genera una multa del 10%, restringe la cuenta y fija un plazo de 72
horas. Una multa vencida pasa la cuenta a `bloqueada_permanente`. La simulacion
deterministica de exito o falla esta expuesta mediante los endpoints auxiliares
`/api/admin`.

Para seleccionar entrega:

```powershell
$envio = @{ tipo = 'envio'; direccionEnvioId = 5101 } | ConvertTo-Json
Invoke-RestMethod -Method Put http://localhost:8080/api/compras/13002/entrega `
  -Headers $headers -ContentType 'application/json' -Body $envio

$retiro = @{ tipo = 'retiro' } | ConvertTo-Json
```

El retiro no cobra envio y registra perdida de cobertura al retirar. Para envio
se usa un costo plano configurable mediante `APP_PURCHASE_SHIPPING_FLAT_COST`,
con valor dev por defecto `5000`. Al aprobar extras, la direccion de envio se
congela como snapshot en `app_entregas`; desde ese momento no se puede cambiar
el modo de entrega de esa compra.

Pago de extras y regularizacion de multa:

```powershell
$pago = @{ medioPagoId = 5001; idempotencyKey = 'extras-demo-13002' } | ConvertTo-Json
Invoke-RestMethod -Method Post http://localhost:8080/api/compras/13002/pagar `
  -Headers $headers -ContentType 'application/json' -Body $pago

$pagoMulta = @{ medioPagoId = 5004; idempotencyKey = 'multa-demo-13001' } | ConvertTo-Json
Invoke-RestMethod -Method Post http://localhost:8080/api/compras/13001/pagar-con-multa `
  -Headers $headersMulta -ContentType 'application/json' -Body $pagoMulta
```

Estados implementados:

```text
adjudicacion_pendiente
multa_activa
pagos_extra_pendientes
pagada
entrega_pendiente
retiro_pendiente
abandonada_por_incumplimiento_pago
abandonada_por_incumplimiento_retiro
completada
```

Los servicios internos tambien permiten cerrar subasta, forzar exito o falla
de pago, vencer multas, completar entrega/retiro y marcar abandono. Los
endpoints admin HTTP para simulaciones, multas, abandono y cierre ya estan
publicados. Los comprobantes de compra y recibos de multa se exponen como
metadata autorizada en `/api/compras/{id}/documentos`; la generacion binaria real
del PDF queda fuera de esta implementacion.

## Consignacion De Bienes

Las cuentas `activa` y `restriccion_multa` pueden consignar. Antes de crear una
solicitud deben tener una cuenta bancaria registrada y al menos un medio de
pago registrado. Para ambos requisitos iniciales alcanza cualquier estado que
no sea `rechazado` ni `eliminado`: no hace falta verificacion manual vigente.
La cuenta bancaria representa el destino de una eventual liquidacion y sus
datos son responsabilidad del usuario.

```text
GET  /api/consignaciones/requisitos
POST /api/consignaciones
POST /api/consignaciones/{id}/documentacion-origen
GET  /api/consignaciones?filtro=activas|rechazadas|vendidas&page=0&size=20
GET  /api/consignaciones/{id}
POST /api/consignaciones/{id}/acuerdo/aceptar
POST /api/consignaciones/{id}/acuerdo/rechazar
POST /api/consignaciones/{id}/devolucion
POST /api/consignaciones/{id}/devolucion/pagar-envio
```

El alta usa `multipart/form-data`, exige aceptación de términos, declaración de
propiedad y origen lícito, y al menos seis imágenes JPEG, PNG o WebP reales.
Antes de aceptar el acuerdo solo existe `app_solicitudes_consignacion`: no se
crean filas legacy en `productos`, `fotos` ni `duenios`.

```powershell
curl.exe -X POST http://localhost:8080/api/consignaciones `
  -H "Authorization: Bearer $accessToken" `
  -F "segmento=arte" -F "categoriaSubasta=comun" -F "aceptaTyC=true" `
  -F "declaracionPropiedadYOrigenLicito=true" `
  -F "titulo=Vasija demo" -F "descripcion=Solicitud manual" `
  -F "fotos=@C:\temp\foto-01.png;type=image/png" `
  -F "fotos=@C:\temp\foto-02.png;type=image/png" `
  -F "fotos=@C:\temp\foto-03.png;type=image/png" `
  -F "fotos=@C:\temp\foto-04.png;type=image/png" `
  -F "fotos=@C:\temp\foto-05.png;type=image/png" `
  -F "fotos=@C:\temp\foto-06.png;type=image/png"
```

La documentación de origen admite PDF o imágenes con validación binaria. Al
subirla vuelve a `pendiente_revision` con documentos pendientes: nunca se aprueba automaticamente.
El backend acepta entre seis y quince fotos. `segmento` es rubro o tema del
bien, por ejemplo arte, joyas, vehiculos o relojeria. `categoriaSubasta` queda
reservada para `comun`, `especial`, `plata`, `oro` o `platino`; si se omite, se
usa `comun` por compatibilidad.

Al aceptar el acuerdo se materializan `productos` y `fotos` legacy. Al publicar
se crea el ítem de catálogo, se congela la comisión del comprador, se suma el
movimiento de puntos y se asigna póliza. Una póliza combinada solo puede reunir
piezas del mismo dueño en la misma subasta.

Si corresponde devolver el bien, elegir retiro no genera pago de envio. Para
elegir envio, el cobro se valida recien al pagar la devolucion: el usuario puede
usar un medio distinto al registrado cuando inicio la consignacion, pero ese
instrumento debe estar `verificado`, con `verificado_hasta` vigente, moneda
compatible y fondos o limite suficientes.
Al pagar ese envio se genera metadata de comprobante de devolucion.

La liquidacion exige una cuenta bancaria destino registrada y no eliminada. No
requiere que su verificacion manual siga vigente y no se bloquea
automaticamente si la empresa la marco como rechazada luego de iniciada la
consignacion; la transferencia se intenta contra la cuenta elegida por el
usuario y queda auditada.
Al liquidar se genera metadata de liquidacion.

Los endpoints admin permiten solicitar y
revisar documentación, aprobar o rechazar revisión digital y física, verificar
al consignador en `duenios`, proponer acuerdos, asignar subasta/póliza,
liquidar ventas y marcar devoluciones incumplidas. El aumento voluntario de
cobertura de póliza queda fuera de la app.

## Stack

- Java 17.
- Spring Boot `4.0.6`.
- Maven Wrapper.
- Spring Web MVC, Spring Security, Bean Validation y Spring Data JPA.
- JWT HS256 con refresh token persistido y revocable.
- PostgreSQL y Flyway.
- WebSocket/STOMP.
- Storage local configurable para binarios y metadata en base.
- H2 efimera solo para tests automatizados.

### Acceso A Datos

Los services coordinan reglas de negocio, transacciones e integraciones. Las
consultas reutilizables o puramente de lectura se concentran en
`repository/app`, incluyendo proyecciones para usuario, compras, admin,
subastas/autorizacion STOMP y disponibilidad de medios de pago.

Se mantienen consultas JDBC puntuales dentro de services cuando forman parte
de una secuencia transaccional estrecha: locks y escrituras de pujas, pagos,
cierre de lote, consignacion y sincronizacion con tablas legacy. Mail y
WebSocket permanecen en sus services/adapters correspondientes.

## Requisitos

- JDK 17 o superior.
- PostgreSQL 14 o superior recomendado.
- PowerShell en Windows para ejecutar `mvnw.cmd`.
- `createdb`, `dropdb` y `psql` son opcionales para validar migraciones
  manualmente desde una base vacia.

## Configuración

El perfil por defecto exige variables de entorno:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/quickbid'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='tu-password'
$env:APP_JWT_SECRET='reemplazar-por-un-secreto-largo'
```

Variables principales:

| Variable | Requerida | Default | Uso |
| --- | --- | --- | --- |
| `DB_URL` | Sí, salvo default `dev` | `jdbc:postgresql://localhost:5432/quickbid` en `dev` | URL JDBC PostgreSQL |
| `DB_USERNAME` | Sí, salvo default `dev` | `postgres` en `dev` | Usuario PostgreSQL |
| `DB_PASSWORD` | Sí | Sin default | Clave PostgreSQL |
| `APP_JWT_SECRET` | Sí | Sin default | Secreto HS256 de al menos 32 caracteres |
| `APP_JWT_ACCESS_TOKEN_MINUTES` | No | `15` | Vigencia access token |
| `APP_JWT_REFRESH_TOKEN_DAYS` | No | `30` | Vigencia refresh token |
| `APP_FILES_STORAGE_PATH` | No | `./uploads` | Directorio de binarios |
| `APP_MAX_IMAGE_SIZE_BYTES` | No | `10485760` | Máximo por imagen |
| `APP_MAX_DOCUMENT_SIZE_BYTES` | No | `10485760` | Máximo por documento |
| `APP_MAX_FILE_SIZE` | No | `10MB` | Límite multipart por archivo |
| `APP_MAX_REQUEST_SIZE` | No | `60MB` | Límite multipart por request |
| `APP_MAIL_ENABLED` | No | `false` | Selecciona SMTP real o mail simulado |
| `APP_MAIL_FROM` | Solo con SMTP | Sin default | Remitente SMTP |
| `APP_MAIL_NOTIFICATIONS_ENABLED` | No | `true` | Emails para eventos de negocio |
| `APP_FRONTEND_BASE_URL` | No | `http://localhost:3000` | Base de links de setup y recuperacion |
| `SPRING_MAIL_HOST` | Solo con SMTP | Sin default | Host SMTP |
| `SPRING_MAIL_PORT` | No | `587` | Puerto SMTP |
| `SPRING_MAIL_USERNAME` | Segun proveedor | Sin default | Usuario SMTP |
| `SPRING_MAIL_PASSWORD` | Segun proveedor | Sin default | Credencial SMTP |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | No | `true` | Autenticacion SMTP |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | No | `true` | STARTTLS SMTP |
| `APP_PURCHASE_SHIPPING_FLAT_COST` | No | `5000` | Envío plano de compras |
| `APP_PURCHASE_COMPLETED_POINTS` | No | `60` | Puntos por compra completada a tiempo |
| `APP_PURCHASE_FINE_GENERATED_POINTS_PENALTY` | No | `90` | Puntos restados al generar multa |
| `APP_PURCHASE_FINE_EXPIRED_POINTS_PENALTY` | No | `250` | Puntos restados al vencer multa |
| `APP_CONSIGNMENT_RETURN_SHIPPING_FLAT_COST` | No | `12000` | Envío de devolución |
| `APP_CONSIGNMENT_PUBLISHED_POINTS` | No | `70` | Puntos por publicación |
| `APP_ADMIN_ENABLED` | No | `false` | Habilita backoffice auxiliar |
| `APP_ADMIN_INTERNAL_KEY` | Solo si admin está activo | Sin default | Clave del header admin |
| `APP_ADMIN_EMPLOYEE_ID` | No | `1002` | Empleado asociado a auditoría admin |

Para desarrollo local puede activarse el perfil `dev`. Este perfil conserva
defaults para URL y usuario, pero no incluye contraseñas ni secretos:

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'
$env:DB_PASSWORD='tu-password'
$env:APP_JWT_SECRET='reemplazar-por-un-secreto-largo'
.\mvnw.cmd spring-boot:run
```

Flyway crea el esquema automáticamente al arrancar sobre una base vacía.
Hibernate usa `ddl-auto=validate`: no crea ni modifica tablas.

Para iniciar el backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Para crear una base vacía con `psql`:

```powershell
createdb -h localhost -U postgres quickbid
```

Para validar una base vacia aislada:

```powershell
createdb -h localhost -U postgres quickbid_flyway_validation
$env:DB_URL='jdbc:postgresql://localhost:5432/quickbid_flyway_validation'
.\mvnw.cmd spring-boot:run
psql -h localhost -U postgres -d quickbid_flyway_validation `
  -c 'SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;'
```

Detener el backend antes de eliminar esa base temporal.

## Migraciones

- `V1__create_legacy_schema.sql`: traducción PostgreSQL del SQL legacy recibido.
- `V2__create_app_schema.sql`: tablas nuevas propias de QuickBid con prefijo
  `app_`.
- `V3__seed_base.sql`: países, empleados, sectores, martillero demo y umbrales
  configurables de categoría.
- `V4__seed_demo_data.sql`: usuarios y escenarios funcionales predecibles para
  desarrollo y pruebas manuales.
- `V5__auth_registration_links.sql`: vinculos entre solicitud aprobada,
  persona y cliente legacy para finalizar el registro.
- `V6__consignment_workflow_support.sql`: estados y referencias `app_*`
  necesarios para revisión documental, devolución, pago de envío y ubicación
  física de consignaciones.
- `V7__payment_method_rejection_reason.sql`: motivo persistido para el rechazo
  manual de medios de pago.
- `V8__registration_retries_and_payment_revalidation.sql`: unicidad parcial para
  reintentos de inscripciones rechazadas.
- `V9__bid_reservations_and_lot_timers.sql`: reservas `app_*` de medios para
  pujas ganadoras activas y columnas de programacion de lote/subasta.
- `V10__purchase_commissions_addresses_documents.sql`: comisiones congeladas de
  comprador/vendedor y snapshot de direccion de entrega.
- `V11__consignment_segment_and_review_flow.sql`: separa `segmento` de
  `categoriaSubasta` en consignaciones y migra datos existentes sin tocar
  tablas legacy.
- `V12__fix_seller_commission_backfill.sql`: corrige únicamente el backfill de
  `app_compras.comision_vendedor` para compras vinculadas a consignaciones,
  usando `comision_vendedor_pct`; no altera comisión comprador ni compras
  legacy sin consignación y evita escrituras innecesarias con
  `IS DISTINCT FROM`.

`V4` usa IDs explícitos en rangos demo y resincroniza las secuencias al final.
Flyway ejecuta cada migración una sola vez por base. Para reiniciar todos los
escenarios durante desarrollo conviene recrear la base.

## Datos Demo

Todas las cuentas operativas demo usan la misma contraseña:

```text
Demo123!
```

La base almacena únicamente su hash BCrypt.

| Email | Estado | Categoría | Uso principal |
| --- | --- | --- | --- |
| `aprobado@quickbid.demo` | `activa` | `plata` | Login normal, medios ARS/USD, inscripciones y pujas |
| `multa@quickbid.demo` | `restriccion_multa` | `especial` | Compra `13001` con multa activa `14001` |
| `bloqueado@quickbid.demo` | `bloqueada_permanente` | `comun` | Flujo de bloqueo posterior al login |
| `consignador@quickbid.demo` | `activa` | `oro` | Bandeja completa de consignaciones |

Además existe la solicitud de registro `pendiente@quickbid.demo` en estado
`pendiente_revision`. Todavía no tiene cuenta operativa ni contraseña.

### Medios De Pago

| ID | Usuario | Tipo | Moneda | Estado |
| --- | --- | --- | --- | --- |
| `5001` | aprobado | Tarjeta Visa terminada en `4242` | `ARS` | `verificado` |
| `5002` | aprobado | Cuenta bancaria | `USD` | `verificado` |
| `5003` | aprobado | Cheque certificado | `ARS` | `pendiente_verificacion` |
| `5004` | multa | Tarjeta Mastercard terminada en `4444` | `ARS` | `verificado` |
| `5005` | consignador | Cuenta bancaria | `ARS` | `verificado` |

Los identificadores sensibles son hashes o tokens simulados. No hay PAN
completos ni CVV almacenados.

### Subastas

| ID | Título | Moneda | Categoría | Estado app | Escenario |
| --- | --- | --- | --- | --- | --- |
| `6001` | Diseño argentino en vivo | `ARS` | `plata` | `en_vivo` | Lote activo `9001`, puja actual `12501` |
| `6002` | Colección internacional USD | `USD` | `especial` | `programada` | Preinscripción y validación de moneda |
| `6003` | Remate clásico finalizado | `ARS` | `comun` | `finalizada` | Compras pagada/parcial y multa |
| `6004` | Selección oro | `ARS` | `oro` | `abierta` | Reglas de puja oro y compra empresa |

Cada subasta tiene catálogo e ítems con precio base. También hay productos y
fotos legacy placeholder.

### Compras Y Multas

| ID | Estado | Escenario |
| --- | --- | --- |
| `13001` | `multa_activa` | Compra de `multa@quickbid.demo`; multa pendiente `14001` por ARS 15.000 |
| `13002` | `pagos_extra_pendientes` | Cobro de adjudicación exitoso; faltan costos posteriores |
| `13003` | `pagos_extra_pendientes` | Compra interna de empresa sin puja |

### Consignaciones

El usuario `consignador@quickbid.demo` tiene solicitudes para probar estos
estados:

| ID | Estado |
| --- | --- |
| `16001` | `pendiente_revision`, con seis fotos |
| `16002` | `documentacion_adicional` |
| `16003` | `revision_fisica` |
| `16004` | `acuerdo_pendiente` |
| `16005` | `publicada` |
| `16006` | `devolucion_pendiente` |
| `16007` | `liquidada`, con documento de liquidación |
| `16008` | `acuerdo_aceptado`, todavía sin publicación |

Las solicitudes previas a la aceptación del acuerdo no tienen producto legacy.

## Traducción Del Legacy

Las tablas legacy no se extienden. La migración inicial aplica únicamente estas
adaptaciones técnicas:

- `identity` de T-SQL pasa a `GENERATED BY DEFAULT AS IDENTITY`.
- `varbinary(max)` pasa a `bytea`.
- `dateAdd(dd, 10, getdate())` pasa a `CURRENT_DATE + 10`.
- Se corrigen separadores ausentes o sobrantes del texto SQL original.
- Los valores corregidos en la fuente actual son `inactivo` y `cerrada`.
- Las columnas acentuadas de `duenios` se normalizan a identificadores ASCII:
  `"verificacionFinanciera"` y `"verificacionJudicial"`.
- Se preservan con comillas los nombres mixtos `"itemsCatalogo"` y
  `"registroDeSubasta"` para evitar que PostgreSQL los convierta a minúsculas.

No se agrega la relación `productos.seguro -> seguros.nroPoliza`, porque no está
declarada en el SQL legacy fuente.

## Esquema App

Las capacidades nuevas viven en tablas `app_*`. La base ya contempla:

- registro por etapas, cuentas, refresh tokens y recuperación con token hasheado;
- categorías y movimientos de puntos;
- metadata de archivos y documentos;
- medios de pago con detalles por tipo, vigencia, límites y borrado lógico;
- extensiones de subasta, snapshot vivo versionado, inscripción y pujas
  idempotentes;
- compras, pagos idempotentes, multas, entregas y direcciones;
- consignación completa, devolución y liquidación;
- notificaciones y auditoría.

Reglas confirmadas para etapas posteriores:

- la validación de un medio de pago dura 5 días hábiles;
- el token de recuperación dura 30 minutos;
- la primera puja puede igualar el precio base;
- oro y platino exigen como mínimo una unidad monetaria por encima de la mejor
  puja;
- la inscripción no crea asistencia: `asistentes` nace al aceptar la primera
  puja del usuario en esa subasta;
- después de aceptar el acuerdo de consignación no corresponde rechazo ni
  devolución.

## Admin Auxiliar

El backoffice auxiliar no tiene frontend propio. Sus rutas viven bajo
`/api/admin/**`, están deshabilitadas por defecto y no aceptan el Bearer de un
usuario común como credencial suficiente. Para activarlas localmente:

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'
$env:APP_ADMIN_ENABLED='true'
$env:APP_ADMIN_INTERNAL_KEY='reemplazar-por-una-clave-interna'
$env:APP_ADMIN_EMPLOYEE_ID='1002'
$adminHeaders = @{ 'X-QuickBid-Admin-Key' = $env:APP_ADMIN_INTERNAL_KEY }
```

La clave se envía únicamente mediante `X-QuickBid-Admin-Key`; no se guarda en
el repositorio ni se imprime en logs. El empleado configurado queda asociado a
las auditorías. En producción estas rutas deben mantenerse deshabilitadas o
protegerse adicionalmente en la infraestructura.

Operaciones disponibles:

```text
GET  /api/admin/solicitudes-registro
GET  /api/admin/solicitudes-registro/{id}
POST /api/admin/solicitudes-registro/{id}/aprobar
POST /api/admin/solicitudes-registro/{id}/rechazar
POST /api/admin/usuarios/{id}/bloquear
POST /api/admin/usuarios/{id}/desbloquear
PATCH /api/admin/usuarios/{id}/puntos
PATCH /api/admin/usuarios/{id}/categoria
GET  /api/admin/medios-pago
POST /api/admin/medios-pago/{id}/verificar
POST /api/admin/medios-pago/{id}/rechazar
POST /api/admin/medios-pago/vencer-expirados
POST /api/admin/vencimientos/procesar
POST /api/admin/multas/vencer-expiradas
POST /api/admin/compras/abandonar-vencidas
POST /api/admin/consignaciones/devoluciones/vencer
POST /api/admin/notificaciones/limpiar
POST /api/admin/subastas
PATCH /api/admin/subastas/{id}
POST /api/admin/subastas/{id}/abrir
POST /api/admin/subastas/{id}/cerrar
POST /api/admin/subastas/{id}/item-activo
POST /api/admin/subastas/{id}/cerrar-lote
POST /api/admin/subastas/timers/procesar
POST /api/admin/subastas/{id}/catalogo/items
POST /api/admin/compras/{id}/simular-pago-exitoso
POST /api/admin/compras/{id}/simular-falla-pago
POST /api/admin/compras/{id}/abandonar?retiro=false
POST /api/admin/multas/{id}/vencer
POST /api/admin/multas/{id}/marcar-pagada
GET  /api/admin/consignaciones
POST /api/admin/consignaciones/{id}/pedir-documentacion
POST /api/admin/consignaciones/{id}/revisar-documentacion
POST /api/admin/consignaciones/{id}/aprobar-revision-digital
POST /api/admin/consignaciones/{id}/marcar-recibida-fisicamente
POST /api/admin/consignaciones/{id}/aprobar-revision-fisica
POST /api/admin/consignaciones/{id}/rechazar-revision-fisica
POST /api/admin/consignaciones/{id}/marcar-devolucion-incompleta
POST /api/admin/consignaciones/{id}/rechazar
POST /api/admin/consignadores/{cuentaId}/verificar-duenio
POST /api/admin/consignaciones/{id}/proponer-acuerdo
POST /api/admin/consignaciones/{id}/asignar-subasta
POST /api/admin/consignaciones/{id}/liquidar
POST /api/admin/seed/base
POST /api/admin/seed/demo-usuarios
POST /api/admin/seed/demo-subastas
POST /api/admin/reset/demo
```

Los endpoints de vencimientos son procesadores auxiliares idempotentes para
operacion manual o cron externo: vencen medios verificados expirados, bloquean
cuentas con multas vencidas, marcan compras con extras impagos por mas de 72
horas, marcan devoluciones de consignacion vencidas por mas de 72 horas y
limpian notificaciones antiguas. No hay scheduler background productivo dentro
del proceso; queda como integracion operativa pendiente.

Ejemplos:

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/solicitudes-registro/1/aprobar `
  -Headers $adminHeaders -ContentType 'application/json' `
  -Body '{"documento":"DNI-DEMO-001","categoriaInicial":"comun"}'
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/medios-pago/5003/verificar `
  -Headers $adminHeaders -ContentType 'application/json' `
  -Body '{"limiteAprobado":150000}'
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/subastas/6004/abrir `
  -Headers $adminHeaders
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/subastas/6004/cerrar-lote `
  -Headers $adminHeaders
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/subastas/6004/cerrar `
  -Headers $adminHeaders
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/compras/13002/simular-pago-exitoso `
  -Headers $adminHeaders -ContentType 'application/json' `
  -Body '{"cuentaId":3001,"medioPagoId":5001,"tipo":"extras"}'
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/multas/14001/marcar-pagada `
  -Headers $adminHeaders
```

Para consignaciones, el flujo manual habitual es listar, revisar o pedir
documentación, marcar recepción física, verificar dueño, proponer acuerdo,
asignar subasta y liquidar. Cada transición valida el estado anterior:

```powershell
Invoke-RestMethod http://localhost:8080/api/admin/consignaciones -Headers $adminHeaders
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/consignaciones/16001/pedir-documentacion `
  -Headers $adminHeaders -ContentType 'application/json' -Body '{"motivo":"Adjuntar factura"}'
Invoke-RestMethod -Method Post http://localhost:8080/api/admin/consignadores/3004/verificar-duenio `
  -Headers $adminHeaders -ContentType 'application/json' `
  -Body '{"financiera":true,"judicial":true,"riesgo":1}'
```

Los endpoints `seed/*` y `reset/demo` solo se habilitan con perfil `dev` o
`test`. Flyway sigue siendo el dueño del seed: estas rutas responden sin
modificar datos. El reset destructivo se evita intencionalmente; para restaurar
todos los escenarios demo se recrea la base y se aplican las migraciones.

## Flujo Recomendado De Prueba

1. Crear una base PostgreSQL vacia y arrancar con perfil `dev`.
2. Hacer login con `aprobado@quickbid.demo` y guardar access/refresh token.
3. Comparar catálogo invitado y autenticado para verificar ocultamiento de
   precios.
4. Inscribirse, ejecutar verificación y consultar puja actual en subasta demo.
5. Pujar usando `clientStateVersion` e `idempotencyKey`.
6. Activar admin localmente y cerrar lote para crear compra.
7. Configurar entrega, pagar extras o simular multa.
8. Recorrer una consignación demo desde revisión hasta publicación y
   liquidación.

Los ejemplos preparados para ejecutar están en
[`docs/api-examples.http`](docs/api-examples.http). Usan variables y datos
ficticios; no contienen secretos locales. Los requests multipart requieren
reemplazar las rutas `./fixtures/*` por archivos binarios válidos del entorno.

## Compilar Y Probar

Los tests usan una base H2 efímera para que la verificación básica del contexto
no dependa de una instalación PostgreSQL local:

```powershell
.\mvnw.cmd clean test
```

La validación real de Flyway debe ejecutarse adicionalmente contra una base
PostgreSQL vacía, porque H2 no reemplaza las pruebas del dialecto productivo.
Se verifico previamente el arranque con perfil `dev` desde cero sobre
PostgreSQL local para `V1` a `V8`. `V9`, `V10`, `V11` y `V12` estan cubiertas
por tests, pero siguen pendientes de validacion Flyway real sobre una base
PostgreSQL vacia antes de la entrega final.

## Decisiones Y Limitaciones Conocidas

- El streaming de video externo queda fuera del alcance del backend.
- La pasarela bancaria es una simulación determinística para pruebas.
- El email SMTP real es opcional. Desarrollo y tests usan por defecto el modo
  simulado, que registra metadata sin imprimir tokens de un solo uso.
- STOMP exige Bearer valido al conectar y autorizacion fina por destino en cada
  suscripcion. Heartbeat, `DISCONNECT` y TTL limpian presencia local. Para un
  despliegue horizontal, la presencia debe moverse a un backend compartido
  como Redis.
- Los documentos de compra y consignacion se listan como metadata autorizada.
  No se publica un endpoint general de descarga de binarios.
- Los vencimientos criticos tienen endpoints admin auxiliares testeados. El
  scheduler background productivo debe conectarse como cron externo o tarea
  `@Scheduled` antes de operar sin backoffice manual.
- El rate limiting vive en memoria por instancia. Para despliegue horizontal
  debe moverse a un backend compartido como Redis.
- Los endpoints admin están apagados por defecto. Para producción requieren una
  protección adicional de infraestructura o un esquema de roles formal.

El estado punto por punto está documentado en
[`docs/CHECKLIST_FINAL.md`](docs/CHECKLIST_FINAL.md).

## Troubleshooting

- `APP_JWT_SECRET must contain at least 32 characters`: definir
  `APP_JWT_SECRET` con un valor largo; no usar defaults compartidos.
- Error de conexión PostgreSQL: verificar `DB_URL`, `DB_USERNAME`,
  `DB_PASSWORD`, servicio local y existencia de la base.
- Error de Hibernate al iniciar: revisar que Flyway haya aplicado `V1` a `V12`
  y consultar `flyway_schema_history`.
- `401 UNAUTHORIZED`: enviar `Authorization: Bearer <accessToken>` vigente.
- `403 ACCOUNT_BLOCKED`: usar otra cuenta demo o regularizar el escenario de
  multa desde admin.
- `403 FORBIDDEN` en `/api/admin/**`: activar `APP_ADMIN_ENABLED=true` y enviar
  `X-QuickBid-Admin-Key`.
- Error SMTP: revisar `APP_MAIL_FROM`, host, puerto, STARTTLS y credenciales. En
  Gmail usar app password; no guardar secretos en archivos versionados.
- `409 BID_OUTDATED_STATE`: volver a consultar `/puja-actual` y reenviar la
  puja con la nueva versión.
- Fallas multipart: respetar MIME real, firma binaria y tamaños configurados.

## Próximos Pasos

Auth, registro, perfil, medios de pago, consulta/inscripcion de subastas, pujas
live, cierre de lote, compras, consignaciones y admin auxiliar ya tienen una
implementacion funcional con pruebas HTTP, concurrencia y realtime. Antes de un
despliegue productivo corresponde resolver las limitaciones listadas arriba.
