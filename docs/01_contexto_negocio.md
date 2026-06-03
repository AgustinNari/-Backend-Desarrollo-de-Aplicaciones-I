# 01 — Contexto de negocio QuickBid

## Dominio

QuickBid es una app móvil para una empresa de subastas/remates presenciales que quiere permitir participación online y consignación de bienes desde la app.

Una subasta es una competencia de ofertas donde gana quien ofrece más dinero. La empresa usa **subasta dinámica ascendente**: los postores conocen las ofertas vigentes y pueden mejorar su oferta mientras el lote/subasta está abierta.

## Objetos principales

- **Usuario/postor/comprador:** persona registrada, aprobada y con cuenta activa en la app. No hay roles separados para comprador/vendedor: un usuario validado puede pujar, comprar y consignar si no tiene restricciones.
- **Invitado:** usuario no autenticado. Puede ver información pública limitada, pero no precios, pujas ni procesos en vivo.
- **Subasta:** evento con fecha, hora, ubicación, categoría, rematador/subastador, moneda y catálogo.
- **Catálogo:** lista pública de ítems/lotes de una subasta.
- **Ítem/lote/producto:** bien subastado. Puede tener varias piezas físicas y varias imágenes. Puede ser arte/diseño con metadatos extra.
- **Puja:** oferta ascendente por un ítem/lote.
- **Compra:** resultado de una puja ganadora o compra interna de la empresa si nadie puja.
- **Medio de pago:** tarjeta, cuenta bancaria o cheque certificado, con moneda, verificación y límites/fondos.
- **Consignación:** solicitud de un usuario para que la empresa subaste un bien propio.
- **Cuenta app:** estructura nueva para login, password, tokens, estado operativo, sesiones, etc.; complementa a `personas`/`clientes` legacy.

## Alcance de la app

Incluye:

- autenticación y registro;
- modo invitado;
- perfil, categoría, puntos, estadísticas e historial;
- medios de pago;
- listado/detalle de subastas y catálogos;
- inscripción/verificación de acceso a subastas;
- pujas en tiempo real;
- compras, documentos, pagos, multas y entrega/retiro;
- consignación completa de bienes;
- notificaciones;
- endpoints admin auxiliares para pruebas y acciones manuales.

No incluye:

- frontend admin formal;
- streaming de video de la subasta;
- derivaciones judiciales externas;
- integración real con pasarelas bancarias, salvo simulación/abstracción para pruebas.

## Modo invitado

El invitado puede ver:

- listado general de subastas;
- detalles generales de una subasta;
- catálogo general;
- detalle descriptivo de ítems/lotes.

El invitado **no puede ver**:

- precio base;
- puja actual;
- historial de pujas;
- qué lote está siendo subastado en vivo;
- estado vivo del proceso;
- montos, valores o datos económicos;
- compras, consignación, perfil, notificaciones, métodos de pago.

Si intenta usar una función protegida, el backend debe responder `401/403` según corresponda y el frontend mostrará pantalla/mensaje de función bloqueada por estar como invitado.

## Usuario autenticado y validado

Un usuario solo puede operar normalmente si:

- completó registro;
- fue aprobado por la empresa;
- creó contraseña;
- tiene cuenta app activa;
- no está bloqueado permanentemente.

Para pujar además debe:

- tener categoría suficiente para la subasta;
- tener al menos un medio de pago verificado;
- usar medio de pago de la misma moneda que la subasta;
- no tener restricción por multa pendiente;
- no estar participando activamente en otra subasta/lote incompatible;
- cumplir límites de puja y fondos/límites de pago.

## Integración con sistema existente

El sistema legacy contiene países, personas, empleados, clientes, dueños, subastadores, subastas, productos, fotos, catálogos, ítems, asistentes, pujos y registros de subasta. La app debe integrarse con esas tablas, pero no puede modificarlas. Para cualquier dato que no encaje, crear tablas nuevas.
