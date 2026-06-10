# 10 — Plan de correcciones backend post revisión

Este archivo lista las correcciones necesarias para alinear el backend actual con las decisiones finales.

Antes de modificar código, leer `AGENTS.md`, `docs/00_decisiones_finales.md`, `docs/API_CONTRATO_FINAL.md` y este archivo.

## Prioridad alta

### Auth

- Cambiar access token a 15 minutos.
- Cambiar refresh token a 30 días.
- Permitir login limitado para `bloqueada_permanente`.
- Login limitado devuelve estado de cuenta, pero no permite navegación real.
- Agregar/ajustar cambio de contraseña desde sesión activa.
- Mantener recuperación por token de mail.
- En etapa 3, aceptar `setup_token`.
- Reenvío de link invalida links anteriores.
- Recuperación/reenvío devuelven respuestas genéricas.

### DNI

- `etapa2` usa `fotoFrenteDni` y `fotoDorsoDni`.
- DNI no acepta PDF.
- JPG/JPEG y PNG mínimos; WebP opcional.

### Inscripciones

- Mantener cierre de inscripción en 60 minutos.
- Permitir medio `pendiente_verificacion`, `verificado` o `vencido`.
- Permitir nuevo intento si inscripción anterior fue rechazada.
- No crear asistencia con inscripción.
- Crear asistencia con primera puja aceptada.
- Inscripción aprobada puede disparar email/notificación de inicio.

### Medios de pago

- Validar/revalidar exige `limiteAprobado`.
- Nunca tratar límite null como infinito.
- Permitir revalidar medio `vencido`.
- Permitir revalidar medio `verificado`, reiniciando 5 días hábiles.
- Agregar job o endpoint admin para pasar medios expirados a `vencido`.
- Principal único por moneda.
- Borrado lógico y bloqueo si hay operaciones pendientes.
- Puntos por validación: +30.

### Puntos

Implementar valores finales: +30 medio verificado, +1 puja aceptada, +80 ganar puja, +60 compra a tiempo, +70 consignación puesta en subasta, +20 extras en plazo, -90 multa generada, -250 multa vencida. Asegurar puntos nunca menores a 0, recálculo de categoría, historial y ajuste admin.

### Pujas

- `idempotencyKey` obligatorio.
- Rechazar duplicados por key.
- Bloquear segunda puja hasta confirmación.
- Cierre automático de lote a 60 s sin superación.
- Delay 60 s antes del siguiente ítem.
- Delay 120 s antes de finalizar subasta tras último ítem.
- Reservar límite mientras puja sea ganadora.
- Liberar reserva si es superada.
- Convertir reserva en consumo real si gana final.
- Mantener bloqueo de puja sobre producto propio.

### Compras y comisiones

- Comisión comprador sobre precio final ofertado.
- Comisión vendedor sobre precio final ofertado.
- Compra empresa por falta de pujas sin comisión comprador.
- Si nadie puja, empresa compra al precio base usando cliente técnico/sistema.
- Si falla cobro inmediato, compra `multa_activa`.
- Artículo + multa se pagan juntos.
- Comisiones/envío en segundo paso.
- Dirección congelada al pagar extras.
- Job o endpoint para abandono por falta de pago de extras.

### Consignación

- Mínimo 6 fotos, máximo 15.
- Rechazar si supera 15.
- Separar `segmento` de `categoriaSubasta`.
- Documentación de origen vuelve a revisión manual, preferentemente `pendiente_revision`.
- Aceptar acuerdo valida `leyoContrato` y `aceptaClausulasPlazos`.
- Producto legacy solo al aceptar acuerdo.
- No permitir rechazo posterior a producto legacy.
- `duenio` no se requiere al iniciar, sí antes de proponer acuerdo.
- Verificador de dueño y revisor de producto pueden ser distintos.

### Direcciones

- Hasta 5 activas.
- Una principal.
- Administración desde perfil.
- Baja lógica.
- Evitar edición destructiva.
- Congelar dirección elegida al pagar envío/comisiones.

### Notificaciones

- Modelo simple `leida/no_leida` salvo que sea fácil agregar archivado.
- Sin archivado desde app.
- Limpiar leídas a 30 días.
- Limpiar no leídas a 90 días.
- No borrar documentos por borrar notificaciones.
- Notificar medio aprobado/rechazado, consignación aprobada/rechazada, multa generada y subasta inscrita por iniciar.
- Evitar persistir eventos de puja demasiado frecuentes.

### Estadísticas e historial

- `GET /api/usuario/estadisticas` aplica `periodo`.
- Soportar `mes`, `trimestre`, `anual`, `total`.
- Separar métricas comprador/postor y vendedor/consignador si es viable.
- Historial incluye ganadas, perdidas y superadas.
- Compras filtran por estado.
- Consignaciones filtran por activas, rechazadas y vendidas.

### Documentos

- Preferir PDFs reales.
- Factura cuando compra queda completamente pagada.
- Recibo de multa cuando multa se paga.
- Acuerdo al aceptar consignación.
- Liquidación al liquidar vendedor.
- Comprobante devolución al pagar envío.
- Usuario ve última versión válida.
- Documentos visibles en app y reenviables por mail si se implementa.

## Prioridad media-alta

- Jobs o endpoints para vencimientos.
- Descargar/reenviar documentos.
- Enums finales documentados y estables.
- WebSocket para eventos live evitando persistencia excesiva.
- Actualizar ejemplos HTTP.

## Criterio de trabajo

- Cambios mínimos y seguros.
- No cambiar tablas legacy.
- Usar migraciones nuevas para esquema `app_*`.
- Agregar/ajustar tests para reglas críticas.
- Si queda parcial, documentarlo explícitamente en README/checklist.
