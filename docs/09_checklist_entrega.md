# 09 — Checklist de entrega

## Base y arquitectura

- [ ] Proyecto compila.
- [ ] Migraciones crean tablas legacy y nuevas.
- [ ] No se modifican tablas legacy más allá de crearlas.
- [ ] Seed base y demo disponibles.
- [ ] Estructura clara de controllers/services/repositories/entities/dto/mapper/security.
- [ ] README con pasos para correr y probar.

## Auth/registro

- [ ] Etapa 1.
- [ ] Etapa 2 con upload.
- [ ] Aprobación/rechazo admin.
- [ ] Token de setup.
- [ ] Etapa 3.
- [ ] Login.
- [ ] Refresh/session si se decidió.
- [ ] Recuperación/cambio de clave.
- [ ] Reenvío de link genérico.
- [ ] Cuenta bloqueada/restringida.

## Usuario

- [ ] Perfil.
- [ ] Categoría, puntos y progreso.
- [ ] Estadísticas.
- [ ] Historial.
- [ ] Notificaciones y marcar leído.
- [ ] Dirección de envío si se implementa.

## Medios de pago

- [ ] Listado.
- [ ] Alta tarjeta.
- [ ] Alta cuenta.
- [ ] Alta cheque con fotos.
- [ ] Verificación/rechazo admin.
- [ ] Principal.
- [ ] Eliminación lógica.
- [ ] Validación de moneda y estado.

## Subastas

- [ ] Listado público/invitado sin precios.
- [ ] Detalle público/invitado sin precios/live.
- [ ] Catálogo público/invitado sin precios.
- [ ] Detalle ítem.
- [ ] Usuario registrado ve precio base si corresponde.
- [ ] Inscripción.
- [ ] Verificación de acceso.
- [ ] Puja actual para autorizado.
- [ ] Pujar con validaciones.
- [ ] Concurrencia protegida.
- [ ] WebSocket/eventos.
- [ ] Cierre de lote y compra.
- [ ] Compra interna empresa si nadie puja.

## Compras/pagos

- [ ] Listado compras.
- [ ] Detalle compra.
- [ ] Pago compra.
- [ ] Falla de pago genera multa.
- [ ] Pago con multa.
- [ ] Bloqueo por vencimiento.
- [ ] Entrega/envío/retiro.
- [ ] Documentos/comprobantes.

## Consignación

- [ ] Requisitos.
- [ ] Crear solicitud con mínimo 6 fotos.
- [ ] Documentación de origen.
- [ ] Listado/detalle.
- [ ] Revisión/admin.
- [ ] Validación de consignador/duenios.
- [ ] Propuesta de acuerdo.
- [ ] Aceptar/rechazar acuerdo.
- [ ] Crear producto legacy solo con acuerdo aceptado.
- [ ] Asignar a subasta/catálogo.
- [ ] Devolución y pago de envío.
- [ ] Liquidación.

## Seguridad/calidad

- [ ] DTOs separados; no devolver entidades.
- [ ] Manejo global de errores.
- [ ] JSON uniforme.
- [ ] Validaciones con Bean Validation + reglas de servicio.
- [ ] Auditoría.
- [ ] Logs útiles.
- [ ] Tests unitarios de reglas críticas.
- [ ] Tests de integración para flujos principales.
