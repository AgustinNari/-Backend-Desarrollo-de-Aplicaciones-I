# QuickBid - smoke WebSocket/STOMP

Los archivos `.http` no prueban STOMP/WebSocket. Para probar realtime necesitás Postman WebSocket, Insomnia, websocat o un cliente STOMP.

## 1. Obtener token

Ejecutá en `00_smoke_readonly.http` el login aprobado y copiá `data.accessToken`.

## 2. Conectar

URL:

```text
ws://localhost:8080/ws
```

Primer frame STOMP a enviar, reemplazando el token. El terminador STOMP es el carácter NULL; en algunos clientes se escribe como `\0` o se agrega automáticamente.

```text
CONNECT
accept-version:1.2
heart-beat:10000,10000
Authorization:Bearer ACCESS_TOKEN_AQUI

\0
```

Respuesta esperada: frame `CONNECTED`.

## 3. Suscribirse a estado de subasta

```text
SUBSCRIBE
id:sub-estado-6001
destination:/topic/subastas/6001/estado

\0
```

## 4. Suscribirse a pujas de item

```text
SUBSCRIBE
id:sub-pujas-9001
destination:/topic/subastas/6001/items/9001/pujas

\0
```

## 5. Suscribirse a colas privadas

```text
SUBSCRIBE
id:sub-notificaciones
destination:/user/queue/notificaciones

\0
```

```text
SUBSCRIBE
id:sub-pujas-privadas
destination:/user/queue/pujas

\0
```

## 6. Disparar eventos

Con la conexión abierta, ejecutá requests de `02_pujas_compras_flujo_exitoso.http`:

- puja aceptada;
- cierre de lote;
- procesar timers si corresponde.

Deberías recibir eventos como `MEJOR_OFERTA_ACTUALIZADA`, `PUJA_ACEPTADA`, `PUJA_SUPERADA`, `LOTE_CERRADO` o equivalentes implementados.

## 7. Casos esperados

- Sin token o token inválido: el CONNECT debe rechazarse.
- Topic arbitrario no documentado: el SUBSCRIBE debe rechazarse.
- Cola privada ajena: debe rechazarse.
- Cuenta `bloqueada_permanente`: aunque pueda hacer login limitado, no debería navegar suscripciones normales.

## Nota

Los eventos específicos de scheduler background/productivo y eventos finos de reservas/timers están documentados como parciales. El core transaccional de pujas/reservas sí se prueba por HTTP + integración.
