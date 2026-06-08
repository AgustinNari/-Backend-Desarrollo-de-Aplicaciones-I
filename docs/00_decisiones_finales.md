# 00 — Decisiones finales QuickBid

Este archivo contiene la verdad final de negocio y técnica posterior a la revisión de inconsistencias entre documentación general, `.md` internos y backend actual.

Si hay conflicto, prevalece este orden:

1. `docs/00_decisiones_finales.md`
2. `docs/API_CONTRATO_FINAL.md`
3. `docs/10_plan_correcciones_backend.md`
4. `docs/01_contexto_negocio.md` a `docs/09_checklist_entrega.md`
5. `README_BACKEND.md` y `quickbid/README.md`
6. `source_material/` como material histórico, no fuente final

## Criterio general

- El backend debe adaptarse razonablemente al contrato original, pero no de forma ciega: si el backend actual tiene una solución mínima, sólida o técnicamente mejor, puede mantenerse si queda documentada.
- Los endpoints admin son auxiliares para pruebas y gestión manual/backoffice, sin frontend propio.
- Las tablas legacy son intocables. Cualquier extensión va a tablas nuevas `app_*`.
- Las diferencias inevitables al traducir SQL legacy a PostgreSQL se documentan como normalizaciones técnicas.

## Auth, registro y cuenta bloqueada

- Login solo con email y contraseña.
- Access token: 15 minutos.
- Refresh token: 30 días, rotativo y revocable.
- Recovery token: 30 minutos, un solo uso.
- Setup/finalización de registro: 48 horas, un solo uso.
- Reenvío de link invalida links anteriores.
- Recuperación/reenvío responden genérico para no revelar si un email existe.
- Etapa 3 usa `setup_token`.
- Deben existir cambio de contraseña desde sesión activa y recuperación por mail.
- Cuenta `bloqueada_permanente`: login limitado solo para pantalla informativa; no navega ni opera.
- Login debe devolver `estadoCuenta` para que el frontend decida pantalla normal/restricción/bloqueo.

## DNI y archivos

- DNI solo imágenes, no PDF.
- Frente y dorso obligatorios como archivos separados.
- Formatos mínimos: JPG/JPEG y PNG.
- WebP es opcional si ya está implementado o no complica.
- Tamaño máximo configurable.

## Invitado

El invitado puede ver subastas, catálogos e ítems, pero nunca precios ni datos vivos.

Nunca ve: precio base, mejor oferta, historial de pujas, cantidad de pujas, ítem actualmente en vivo ni estado interno de lote.

Sí ve: descripciones, imágenes, datos no económicos y si una subasta está activa o futura sin progreso interno.

## Inscripción a subastas

- Cierra 60 minutos antes del inicio. No usar 30 minutos.
- Puede hacerse con medio `pendiente_verificacion`, `vencido` o `verificado`.
- Si requiere validación/revalidación, queda pendiente de revisión manual.
- Una inscripción rechazada no bloquea nuevo intento con otro medio.
- La inscripción rechazada puede conservarse como historial.
- La inscripción no crea asistencia.
- La asistencia se crea con la primera puja aceptada.
- Un medio validado por inscripción o validación inicial sirve para cualquier subasta de la misma moneda mientras siga vigente.
- La inscripción aprobada puede generar notificación/email de inicio.

## Medios de pago

Tipos: tarjeta, cuenta bancaria, cheque certificado.

Estados finales: `pendiente_verificacion`, `verificado`, `rechazado`, `vencido`, `eliminado`.

Reglas:

- Verificación vigente: 5 días hábiles.
- Al vencer, pasa a `vencido`, idealmente por job.
- Admin puede revalidar medio `vencido` sin recarga.
- Admin puede revalidar medio `verificado`, reiniciando los 5 días hábiles.
- Validar/revalidar exige `limiteAprobado` obligatorio.
- Límite nulo nunca significa infinito.
- Para MVP, tarjetas/cuentas no tienen reinicio mensual/rolling automático; la empresa ajusta límite al validar/revalidar.
- Cheques consumen contra monto fijo sin reinicio mensual.
- Único principal por moneda; máximo uno ARS y uno USD.
- No editar medios salvo gestionar la condición de principal.
- Eliminación siempre lógica y bloqueada si hay operaciones pendientes.
- La validación suma puntos solo al aprobar.

## Puntos, reputación y categorías

Valores finales:

- Medio verificado: +30
- Puja aceptada: +1
- Ganar puja: +80
- Compra completada/pagada a tiempo: +60
- Consignación aceptada y puesta en subasta: +70
- Pagar comisiones/envío en plazo: +20
- Generar multa: -90
- Multa vencida sin pago: -250

Reglas:

- Puntos nunca bajan de 0.
- Categoría sube/baja automáticamente según puntos.
- Admin puede ajustar puntos.
- Ajuste manual recalcula categoría.
- Guardar historial de movimientos.

Rangos:

- `comun`: 0–249
- `especial`: 250–699
- `plata`: 700–1499
- `oro`: 1500–2999
- `platino`: 3000+

## Pujas y realtime

- Primera puja puede igualar precio base.
- `oro`/`platino`: no aplican 1% ni 20%; solo superar por 1 unidad.
- Resto: mínimo = mejor puja + 1% precio base; máximo = mejor puja + 20% precio base.
- Prohibido pujar por producto propio.
- Prohibido tener puja ganadora activa en otra subasta.
- Se puede ver live aunque no pueda pujar por falta de medio/categoría.
- Cierre automático de lote: 60 segundos sin superación.
- Delay recomendado al siguiente lote: 60 segundos.
- Delay recomendado al cierre final de subasta: 120 segundos.
- `idempotencyKey` obligatorio.
- Backend rechaza duplicados/concurrentes con misma key.
- Frontend bloquea otra puja hasta confirmación.
- Mientras una puja es ganadora, reserva/resta capacidad del medio.
- Si es superada, libera la reserva.
- Si gana final, reserva pasa a consumo real.
- Eventos frecuentes de puja superada priorizan WebSocket, no notificación persistente.

## Compras, multas y pagos

- `registroDeSubasta` puede representar adjudicación comercial, no pago completo, si integra mejor con legacy.
- Si falla cobro inmediato, compra queda `multa_activa`.
- Multa: 10% del valor ofertado.
- Artículo + multa se pagan juntos.
- Plazo: 72 horas.
- Si vence sin pago, cuenta `bloqueada_permanente`.
- Pagar artículo + multa quita `restriccion_multa` si no quedan otras multas.
- Comisiones/envío se pagan en segundo paso.
- Si no paga comisiones/envío en plazo, empresa puede quedarse con dinero ya pagado e ítem.
- Preferir job automático para abandono; endpoint admin solo auxiliar.
- Modalidades: retiro personal y envío a domicilio.
- Tras pagar extras, no se cambia modalidad.
- Dirección se congela al pagar extras.
- Comisión comprador y vendedor sobre precio final ofertado.
- Si compra empresa por falta de pujas, no hay comisión comprador.
- Si nadie puja, empresa compra siempre al precio base con cliente técnico/sistema.

## Documentos y PDFs

Documentos principales: factura/comprobante de compra, recibo de multa, acuerdo de consignación, liquidación de venta, comprobante de envío de devolución.

- Preferir PDFs reales.
- Factura/comprobante cuando compra queda completamente pagada.
- Recibo de multa solo al pagar multa.
- Acuerdo al aceptar consignación.
- Liquidación al pagar/liquidar vendedor.
- Comprobante devolución al pagar envío de devolución.
- Usuario ve última versión válida.
- Pueden conservarse versiones históricas internamente si no complica.
- Pueden verse en app y enviarse por mail.
- No se borran automáticamente.

## Consignación

- Mínimo 6 fotos, máximo 15; si supera 15, rechazar.
- `segmento` = rubro/tema del bien.
- `categoria`/`categoriaSubasta` = común/especial/plata/oro/platino.
- Separar `segmento` y `categoriaSubasta`.
- Documentación de origen vuelve a revisión manual; preferir `pendiente_revision` con auditoría.
- Puede existir `rechazo_revision_fisica` antes de `devolucion_pendiente`.
- Rechazo de acuerdo o revisión física: `devolucion_pendiente`.
- Producto legacy se crea recién al aceptar acuerdo.
- Creado producto legacy, ya no puede rechazarse.
- Una solicitud = un bien.
- “Colección” es solo nombre/título comercial de subasta, no entidad especial.
- No se requiere `duenio` legacy para iniciar; sí antes de proponer acuerdo.
- Verificador del dueño puede ser distinto del revisor del producto.

## Direcciones de envío

- Hasta 5 direcciones activas por usuario.
- Una principal.
- Administración desde perfil.
- Domicilio legal puede ser distinto.
- Compra congela dirección elegida al pagar extras.
- Tratar como medios de pago: evitar edición destructiva, usar baja lógica y preservar consistencia histórica.

## Notificaciones

- Modelo simple `leida/no_leida` suficiente para MVP.
- No hace falta archivar desde app.
- Leídas se eliminan luego de 30 días.
- No leídas se eliminan luego de 90 días.
- Job de limpieza si es razonable.
- Borrar notificaciones no borra documentos.
- Notificar aprobación/rechazo de medio, aprobación/rechazo de consignación, multa generada y subasta inscrita por iniciar.
- Evitar persistir eventos de puja demasiado frecuentes.

## Estadísticas, historial y filtros

- Estadísticas: `mes`, `trimestre`, `anual`, `total`.
- `periodo` debe afectar cálculos.
- Separar comprador/postor y vendedor/consignador si es viable.
- Historial: pujas ganadas, perdidas y superadas; paginado.
- Compras filtran por estado.
- Consignaciones filtran por activas, rechazadas y vendidas.

## Jobs y vencimientos

Preferir jobs automáticos para vencer medios verificados, bloquear cuentas con multas vencidas, marcar compras abandonadas, marcar devoluciones vencidas y limpiar notificaciones. Si no hay job, endpoint admin auxiliar para procesar/simular.

El ciclo live de subastas usa un scheduler interno configurable mediante
`app.auctions.scheduler-enabled` y `app.auctions.scheduler-delay-ms`. La
retención ganadora se persiste en `app_subasta_estado_vivo.retencion_hasta`; el
cierre reutiliza el flujo transaccional e idempotente de adjudicación existente.

## Estados finales mínimos

- Medio pendiente: `pendiente_verificacion`
- Cuenta con multa activa: `restriccion_multa`
- Cuenta bloqueada por multa vencida: `bloqueada_permanente`

El backend puede tener estados internos más detallados, pero debe exponer estados estables al frontend.
