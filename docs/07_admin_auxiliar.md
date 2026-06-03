# 07 — Endpoints admin auxiliares

No hay frontend admin formal. Estos endpoints sirven para pruebas, seed/demo y acciones manuales de la empresa. Deben estar protegidos por token admin, header interno o perfil de desarrollo. No exponer sin seguridad.

Base sugerida: `/api/admin`.

## Registro y usuarios

- `GET /api/admin/solicitudes-registro`
- `GET /api/admin/solicitudes-registro/{id}`
- `POST /api/admin/solicitudes-registro/{id}/aprobar`
- `POST /api/admin/solicitudes-registro/{id}/rechazar`
- `POST /api/admin/usuarios/{id}/bloquear`
- `POST /api/admin/usuarios/{id}/desbloquear`
- `PATCH /api/admin/usuarios/{id}/puntos`
- `PATCH /api/admin/usuarios/{id}/categoria`

## Medios de pago

- `GET /api/admin/medios-pago?estado=pendiente_verificacion`
- `POST /api/admin/medios-pago/{id}/verificar`
- `POST /api/admin/medios-pago/{id}/rechazar`

## Subastas y catálogos

- `POST /api/admin/subastas`
- `PATCH /api/admin/subastas/{id}`
- `POST /api/admin/subastas/{id}/abrir`
- `POST /api/admin/subastas/{id}/cerrar`
- `POST /api/admin/subastas/{id}/item-activo`
- `POST /api/admin/subastas/{id}/cerrar-lote`
- `POST /api/admin/subastas/{id}/catalogo/items`

## Pujas y pagos demo

- `POST /api/admin/compras/{id}/simular-pago-exitoso`
- `POST /api/admin/compras/{id}/simular-falla-pago`
- `POST /api/admin/multas/{id}/vencer`
- `POST /api/admin/multas/{id}/marcar-pagada`

## Consignaciones

- `GET /api/admin/consignaciones`
- `POST /api/admin/consignaciones/{id}/pedir-documentacion`
- `POST /api/admin/consignaciones/{id}/rechazar`
- `POST /api/admin/consignaciones/{id}/marcar-recibida-fisicamente`
- `POST /api/admin/consignadores/{cuentaId}/verificar-duenio`
- `POST /api/admin/consignaciones/{id}/proponer-acuerdo`
- `POST /api/admin/consignaciones/{id}/asignar-subasta`
- `POST /api/admin/consignaciones/{id}/liquidar`

## Seed y escenarios

- `POST /api/admin/seed/base`
- `POST /api/admin/seed/demo-subastas`
- `POST /api/admin/seed/demo-usuarios`
- `POST /api/admin/reset/demo` solo en perfil dev/test.

## Reglas admin

- Toda acción admin debe quedar en `app_auditoria`.
- Los endpoints admin pueden ser más prácticos que perfectos, pero no deben violar invariantes de negocio.
- Aunque no exista UI admin, deben permitir probar el flujo completo sin editar SQL manualmente para cada paso.
