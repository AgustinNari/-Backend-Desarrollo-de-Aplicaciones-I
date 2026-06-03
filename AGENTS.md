# AGENTS.md — QuickBid Backend

Este repositorio debe implementar el backend completo de **QuickBid**, una app móvil de subastas dinámicas ascendentes, compras, pagos, consignación de bienes, perfil, estadísticas y notificaciones.

## Objetivo del agente/Codex

Construir un backend sólido, completo y mantenible para que el frontend móvil pueda consumirlo. El backend debe respetar al máximo los endpoints, pantallas, reglas de negocio, restricciones y decisiones ya definidas en la documentación adjunta.

## Lectura obligatoria antes de tocar código

1. `README_BACKEND.md` — visión general, stack sugerido, cómo correr/probar y decisiones asumidas.
2. `docs/01_contexto_negocio.md` — dominio funcional y reglas centrales de la app.
3. `docs/02_reglas_negocio.md` — permisos, estados, pujas, compras, multas, consignación, categorías y plazos.
4. `docs/03_api_contract.md` — contrato de endpoints públicos y auxiliares.
5. `docs/04_modelo_datos.md` — tablas legacy intocables, tablas nuevas recomendadas y relaciones.
6. `docs/05_flujos_backend.md` — flujos transaccionales esperados.
7. `docs/06_realtime_websocket.md` — eventos en tiempo real para subastas, pujas y notificaciones.
8. `docs/07_admin_auxiliar.md` — endpoints internos para pruebas/manual backoffice sin UI.
9. `docs/08_validaciones_errores_seguridad.md` — validaciones, seguridad, errores, auditoría y archivos.
10. `docs/09_checklist_entrega.md` — checklist de completitud.
11. `source_material/` — material original completo recibido. Si existe conflicto entre un resumen y el material original, usar el material original y dejar una nota en el README o en comentarios de implementación.

## Reglas máximas de implementación

- No modificar, extender ni agregar columnas/valores a las tablas del sistema existente/legacy. Si hace falta representar nueva información, crear tablas nuevas relacionadas.
- El backend debe crear también las tablas legacy porque la base actual del proyecto puede estar vacía, pero debe crearlas respetando la estructura enviada.
- Mantener los endpoints públicos ya definidos lo más intactos posible. Solo cambiar o agregar cuando sea necesario para que el backend sea correcto, seguro o consistente.
- Los endpoints de admin son auxiliares para pruebas y operación manual. No hay frontend admin formal.
- Salvo auth, registro inicial y vistas públicas/invitado, todo endpoint requiere token de autorización.
- Responder siempre con JSON uniforme: `data`, `message` y opcionalmente `errors`; el estado real se expresa por HTTP status code.
- Implementar validaciones de negocio en backend aunque también puedan existir en frontend.
- Usar transacciones para pujas, pagos, cierre de lote/subasta, aprobación de registro, aceptación de acuerdo de consignación y cualquier operación que escriba en varias tablas.
- Implementar control de concurrencia fuerte en pujas y pagos. Nunca permitir dos pujas simultáneas del mismo usuario sin confirmación previa ni aceptar pujas basadas en un estado viejo.
- Mantener auditoría de acciones críticas.
- El streaming de video queda fuera del alcance. No hace falta integrarlo ni mencionarlo dentro de la app.

## Stack y estructura sugerida

Stack recomendado si el proyecto no impone otro:

- Java 17+
- Spring Boot 3+
- Maven
- Spring Web
- Spring Security + JWT
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway para migraciones
- WebSocket/STOMP para subastas en vivo
- Multipart + BLOB para imágenes y documentos
- Postman para prueba de endpoints

Estructura sugerida dentro de `src/main/java`:

```text
controller/
  auth/
  usuario/
  subastas/
  compras/
  consignaciones/
  admin/
service/
  interface/
  implementation/
repository/
entity/
  legacy/
  app/
  enums/
dto/
  request/
  response/
  create/
  update/
mapper/
exception/
security/
config/
websocket/
audit/
storage/
```

Puede ajustarse si mejora la implementación, pero documentar las decisiones en el README.

## Definición de terminado

El backend se considera completo cuando:

- Compila y arranca desde cero.
- Crea/migra la base vacía con tablas legacy + tablas nuevas.
- Tiene datos semilla suficientes para probar países, empleados/verificadores, subastadores, sectores, subastas, catálogos, ítems, métodos de pago y usuarios demo.
- Implementa endpoints públicos definidos y endpoints admin auxiliares razonables.
- Implementa autenticación, refresh/session handling si se decide usarlo, recuperación de contraseña y flujo de registro completo.
- Implementa modo invitado solo para lectura pública sin precios ni datos live.
- Implementa flujo de subastas con validaciones de categoría, método de pago, moneda, monto mínimo/máximo, fondos/límites, una subasta activa por usuario y WebSocket.
- Implementa compras, entrega/retiro, pagos, multas, bloqueo por incumplimiento y documentos/comprobantes.
- Implementa consignación end-to-end hasta aceptación/rechazo, publicación, devolución/liquidación y documentación.
- Tiene manejo consistente de errores, auditoría, logs y pruebas mínimas.
