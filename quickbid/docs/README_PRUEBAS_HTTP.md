# QuickBid - guía de pruebas HTTP locales

Estos archivos están pensados para VS Code REST Client y fueron alineados con el backend actual después de las reglas finales: `setup_token`, `fotoFrenteDni`/`fotoDorsoDni`, direcciones múltiples, `limiteAprobado`, reintentos de inscripción, `idempotencyKey` obligatorio, comisiones comprador/vendedor corregidas, `segmento` separado de `categoriaSubasta`, jobs/admin auxiliares y filtros nuevos.

## Arranque recomendado

Desde `quickbid`:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_URL="jdbc:postgresql://localhost:5432/baseQuickbid"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="TU_PASSWORD_POSTGRES"
$env:APP_JWT_SECRET="quickbid-secret-local-de-prueba-super-largo-123456"
$env:APP_ADMIN_ENABLED="true"
$env:APP_ADMIN_INTERNAL_KEY="test-admin-key"
$env:APP_MAIL_ENABLED="false"
.\mvnw.cmd spring-boot:run
```

## Cuándo resetear la base

`00_smoke_readonly.http` puede correrse varias veces. Los demás modifican datos. Para evitar conflictos, lo más simple es usar una base limpia por cada archivo de flujo:

- `01_auth_registro_medios.http`: crea direcciones, medios y solicitud de registro. Resetear antes de repetir completo.
- `02_pujas_compras_flujo_exitoso.http`: cierra lote `6001`; resetear antes de repetir.
- `03_multas_alternativas.http`: elegir una sola rama A o B por base limpia.
- `04_consignacion_flujo_nuevo_feliz.http`: crea una consignación nueva y la avanza; resetear antes de repetir completo.
- `05_consignacion_seeds_ramas_independientes.http`: son ramas independientes sobre seeds; no ejecutar todas como flujo único si querés resultados determinísticos.
- `98_admin_jobs_smoke.http`: procesa vencimientos/limpiezas; conviene correrlo al final de un ciclo.
- `99_mail_real_smoke.http`: requiere reiniciar backend con SMTP real.

Para recrear rápido la base local:

```powershell
# Detener backend primero.
$env:PGPASSWORD="TU_PASSWORD_POSTGRES"
& "C:\Program Files\PostgreSQL\18\bin\dropdb.exe" -h localhost -U postgres --if-exists baseQuickbid
& "C:\Program Files\PostgreSQL\18\bin\createdb.exe" -h localhost -U postgres baseQuickbid
```

Luego volver a levantar el backend para que Flyway aplique V1..V12.

## WebSockets

Los `.http` no prueban STOMP/WebSocket. Usá `06_websocket_stomp_smoke.md` con Postman WebSocket, websocat o un cliente STOMP. Primero hacé login y copiá el `accessToken`.
