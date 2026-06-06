# 06 — Realtime y WebSocket

## Objetivo

Las subastas y pujas requieren actualización en tiempo real. El backend debe proveer WebSocket/STOMP o mecanismo equivalente.

## Canales sugeridos

- `/topic/subastas/{subastaId}/estado`
- `/topic/subastas/{subastaId}/items/{itemCatalogoId}/pujas`
- `/user/queue/notificaciones`
- `/user/queue/pujas`

## Eventos sugeridos

### `SUBASTA_ESTADO_ACTUALIZADO`

Payload:

```json
{
  "subastaId": 1,
  "estado": "en_vivo",
  "itemActivoId": 20,
  "version": 12
}
```

### `PUJA_ACEPTADA`

Evento privado para el usuario cuya puja fue aceptada. El topic compartido
publica `MEJOR_OFERTA_ACTUALIZADA` con alias, nunca el resultado privado.

```json
{
  "subastaId": 1,
  "itemCatalogoId": 20,
  "pujaId": 99,
  "monto": 15100,
  "moneda": "ARS",
  "postorAlias": "Postor #15",
  "version": 13,
  "createdAt": "..."
}
```

### `PUJA_SUPERADA`

Evento privado para el usuario superado.

### `PUJA_RECHAZADA`

Privado:

```json
{
  "requestId": "uuid-client",
  "motivo": "OFERTA_SUPERADA",
  "message": "Tu puja no fue aceptada porque ya existe una oferta mayor."
}
```

### `LOTE_CERRADO`

```json
{
  "subastaId": 1,
  "itemCatalogoId": 20,
  "ganador": true,
  "compraId": 55
}
```

## Reglas realtime

- Nunca enviar precios/montos a invitados.
- Solo usuarios registrados/aprobados pueden suscribirse a canales live con montos.
- El handshake `/ws` puede abrir el transporte sin JWT HTTP; la sesion todavia
  no queda autenticada ni puede suscribirse.
- Cada `CONNECT` exige `Authorization: Bearer <accessToken>` como header nativo
  STOMP, compatible con `connectHeaders` de `@stomp/stompjs`.
- Cada `SUBSCRIBE` revalida cuenta,
  destino, existencia de subasta y pertenencia del item si corresponde.
- Antes de entregar cada evento live o privado, revalidar el estado actual de
  la cuenta asociada a la sesion para cortar bloqueos posteriores al subscribe.
- No exigir inscripcion previa, categoria suficiente ni medio verificado para
  mirar live. Esas reglas afectan la accion REST de pujar, no la visualizacion.
- Una cuenta con `restriccion_multa` puede mirar live y recibir sus colas
  privadas, pero no pujar. Cuentas bloqueadas o deshabilitadas no acceden.
- Publicar mejor oferta con alias como `Postor #15`, nunca nombre o email.
- Enviar `PUJA_ACEPTADA`, `PUJA_SUPERADA` y `PUJA_RECHAZADA` solo por colas
  privadas del usuario correspondiente.
- La validación final siempre ocurre en REST/backend transaccional, no en WebSocket cliente.
- Los eventos deben incluir `version` para que el frontend detecte estado viejo.
- Negociar heartbeat y limpiar presencia efimera por `DISCONNECT` y TTL. Para
  despliegue horizontal, mover la presencia local a un backend compartido.

## Alternativa simple

Si WebSocket se complica, implementar polling para datos no críticos, pero **las pujas deberían usar WebSocket o al menos control transaccional fuerte con refresh frecuente**. La recomendación principal sigue siendo WebSocket.

---

## Addendum post revisión realtime

Eventos/timers recomendados:

- `LOTE_CIERRE_PROGRAMADO`
- `LOTE_CERRADO`
- `PROXIMO_LOTE_PROGRAMADO`
- `LOTE_ACTIVADO`
- `SUBASTA_CIERRE_PROGRAMADO`
- `SUBASTA_FINALIZADA`
- `RESERVA_MEDIO_ACTIVA`
- `RESERVA_MEDIO_LIBERADA`
- `RESERVA_MEDIO_CONSUMIDA`

Reglas finales: cierre automático de lote a 60 s sin superación, delay de 60 s antes del siguiente lote y delay de 120 s antes de finalizar subasta completa. Eventos frecuentes como puja superada deben priorizar WebSocket antes que notificación persistente.
