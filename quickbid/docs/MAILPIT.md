# QuickBid Mailpit y deep links Android

Esta guia valida los mails reales de auth en local usando Mailpit y deep links
Android. El modo seguro por defecto sigue siendo `APP_MAIL_ENABLED=false`, que
usa el servicio simulado y no intenta SMTP.

## Levantar Mailpit

```powershell
docker run -d --restart unless-stopped --name mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
```

Bandeja:

```text
http://localhost:8025
```

SMTP:

```text
localhost:1025
```

## Variables backend

```powershell
$env:APP_MAIL_ENABLED='true'
$env:APP_MAIL_FROM='no-reply@quickbid.demo'
$env:APP_FRONTEND_BASE_URL='quickbid://auth'
$env:SPRING_MAIL_HOST='localhost'
$env:SPRING_MAIL_PORT='1025'
$env:SPRING_MAIL_USERNAME=''
$env:SPRING_MAIL_PASSWORD=''
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH='false'
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE='false'
```

Arranque local habitual:

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'
$env:DB_PASSWORD='tu-password'
$env:APP_JWT_SECRET='reemplazar-por-un-secreto-largo'
.\mvnw.cmd spring-boot:run
```

No guardar credenciales SMTP en el repo. Para Mailpit local no se requiere auth
ni STARTTLS.

## Links generados

Con `APP_FRONTEND_BASE_URL=quickbid://auth`, los mails de auth generan:

```text
quickbid://auth/completar-registro?token=<token-url-encoded>
quickbid://auth/recuperar-clave?token=<token-url-encoded>
```

El token plano solo aparece dentro del link enviado por mail. La base de datos
guarda hashes y los logs no imprimen tokens crudos.

## Flujo de aprobacion de registro

1. Crear una solicitud con `POST /api/auth/registro/etapa1`.
2. Subir DNI/documentacion requerida con `POST /api/auth/registro/etapa2`.
3. Aprobar desde admin con `POST /api/admin/solicitudes-registro/{id}/aprobar`.
4. Abrir Mailpit en `http://localhost:8025`.
5. Abrir el link `quickbid://auth/completar-registro?token=...`.
6. Completar la clave en la app.

El envio SMTP ocurre dentro de la transaccion de aprobacion. Si SMTP falla, la
aprobacion responde error controlado y no queda un token nuevo persistido.

## Reenvio de link de registro

1. Pedir reenvio con `POST /api/auth/registro/reenviar-link`.
2. Revisar Mailpit.
3. Confirmar que llego un mail nuevo con
   `quickbid://auth/completar-registro?token=...`.

La respuesta es generica para evitar enumeracion de emails. Si corresponde, el
backend invalida el link anterior reemplazando el hash por uno nuevo.

## Recuperacion de clave

1. Pedir recuperacion con `POST /api/auth/recuperar-clave`.
2. Revisar Mailpit.
3. Abrir `quickbid://auth/recuperar-clave?token=...`.
4. Cambiar la clave.
5. Iniciar sesion con la clave nueva.

La respuesta tambien es generica. El token dura 30 minutos y es de un solo uso.

## Deep links Android

Aplicar reverse para backend y Metro en pruebas locales:

```powershell
adb reverse tcp:8080 tcp:8080
adb reverse tcp:8081 tcp:8081
```

Probar links con token falso:

```powershell
adb shell am start -W -a android.intent.action.VIEW -d "quickbid://auth/completar-registro?token=abc"
adb shell am start -W -a android.intent.action.VIEW -d "quickbid://auth/recuperar-clave?token=abc"
```

Con `abc`, la app debe abrir la pantalla correcta y mostrar un error controlado
si se intenta usar el token.

## Errores comunes

- No aparece mail: revisar que Mailpit este corriendo y que el backend apunte a
  `SPRING_MAIL_HOST=localhost` y `SPRING_MAIL_PORT=1025`.
- El backend usa simulado: revisar `APP_MAIL_ENABLED=true`.
- El link no abre la app: recompilar Android y confirmar el intent-filter del
  manifest para scheme `quickbid` y host `auth`.
- Token invalido: puede estar vencido, usado o pertenecer a un link anterior.
- Android no conecta al backend local: aplicar `adb reverse tcp:8080 tcp:8080`.
- Metro no carga: aplicar `adb reverse tcp:8081 tcp:8081`.
