> **Actualización post revisión:** este paquete ahora incluye `docs/00_decisiones_finales.md`, `docs/API_CONTRATO_FINAL.md` y `docs/10_plan_correcciones_backend.md`. Esos archivos son la fuente final para Codex.


# Índice del paquete QuickBid Backend para Codex

Este paquete está pensado para copiarlo dentro del repositorio del backend o entregárselo completo a Codex.

## Archivos principales

- `AGENTS.md`: instrucciones centrales para Codex/agentes.
- `README_BACKEND.md`: visión general, stack recomendado, cómo correr, seed y decisiones asumidas.
- `docs/01_contexto_negocio.md`: dominio, alcance, modo invitado, usuario autenticado e integración legacy.
- `docs/02_reglas_negocio.md`: reglas detalladas de cuenta, categorías, pujas, compras, multas, consignación, seguros y notificaciones.
- `docs/03_api_contract.md`: endpoints públicos y contrato de respuesta.
- `docs/04_modelo_datos.md`: tablas legacy, tablas nuevas recomendadas y transacciones.
- `docs/05_flujos_backend.md`: pasos de flujos transaccionales.
- `docs/06_realtime_websocket.md`: canales y eventos realtime.
- `docs/07_admin_auxiliar.md`: endpoints internos para pruebas/manual backoffice.
- `docs/08_validaciones_errores_seguridad.md`: validaciones, errores, seguridad, archivos y auditoría.
- `docs/09_checklist_entrega.md`: checklist para verificar completitud.

## Material fuente original

La carpeta `source_material/` incluye los textos originales completos recibidos. Sirve para que Codex no pierda detalles y pueda consultar el contexto exacto si algún resumen es insuficiente.

## Uso recomendado con Codex

1. Copiar todo este paquete en la raíz del backend.
2. Pedirle a Codex que lea primero `AGENTS.md`.
3. Luego pedir implementación incremental: migraciones/modelo, auth, medios de pago, subastas/pujas, compras/pagos, consignaciones, admin y tests.
4. En cada cambio, exigir que respete `docs/09_checklist_entrega.md`.
