# 02 — Reglas de negocio

## Estados de cuenta

### En registro temporal (`solicitudes_registro`)

- `pendiente_etapa2`: etapa 1 creada, faltan fotos DNI.
- `pendiente_revision`: fotos subidas, revisión manual pendiente.
- `rechazado`: empresa rechazó la solicitud.
- `aprobada_pendiente_finalizacion`: empresa aprobó y emitió token para crear clave.
- `completada`: usuario creó clave y ya existe cuenta operativa.
- `expirada`: opcional, si se implementa vencimiento automático.

### En cuenta operativa (`cuentas_app`)

- `activa`: acceso normal.
- `restriccion_multa`: puede navegar y usar funciones generales, pero no puede pujar ni inscribirse a subastas hasta pagar multa + obligación pendiente.
- `bloqueada_permanente`: bloqueo total. Tras login solo puede ver pantalla informativa de bloqueo, sin navegación real.
- `deshabilitada_admin`: suspensión manual por la empresa.

## Categorías y puntos

Categorías oficiales:

1. `comun`
2. `especial`
3. `plata`
4. `oro`
5. `platino`

La categoría inicial la asigna la empresa al aprobar al usuario. Luego puede subir o bajar según puntos.

Acciones que suman puntos:

- realizar pujas válidas;
- ganar pujas;
- concretar compras luego de una puja ganadora;
- consignar artículos que finalmente sean aceptados, publicados/asignados a subasta;
- registrar nuevos medios de pago, solo cuando sean verificados por la empresa.

Acciones que restan puntos:

- generación de multa por falla de pago;
- vencimiento/incumplimiento de multa;
- eventos graves asociados a pagos no cumplidos.

Regla recomendada:

- calcular categoría desde puntos totales usando tabla configurable `app_categoria_puntos`;
- permitir override/admin de puntos o categoría si hace falta para pruebas;
- registrar siempre en `app_movimientos_puntos` el motivo, referencia y delta.

## Acceso a subastas

Un usuario puede acceder a una subasta para verla con detalles completos si:

- está registrado y aprobado;

Un usuario puede pujar si además:

- su categoría es mayor o igual a la categoría de la subasta.
- tiene medio de pago verificado;
- el medio de pago coincide con la moneda de la subasta;
- no tiene multa/restricción activa;
- no está conectado/participando en más de una subasta a la vez;
- la subasta y el lote están abiertos para puja;
- el monto es válido;
- su garantía/límite/fondos disponibles alcanzan.

Cualquier usuario registrado y aprobado puede acceder al streaming externo, pero el streaming queda fuera del backend/app.

## Moneda

- Cada subasta tiene una sola moneda (`ARS` o `USD`).
- No existen subastas bimonetarias.
- Las pujas, pagos, comisiones, multas, facturas y documentos asociados usan la moneda de la subasta.
- No se puede usar medio de pago de ARS en subasta USD ni viceversa.

## Reglas de puja

Para categorías de subasta `comun`, `especial` y `plata`:

- Puja mínima = mejor oferta actual + 1% del precio base.
- Puja máxima = mejor oferta actual + 20% del precio base.
- Si no hay mejor oferta, tomar precio base como referencia inicial según criterio del backend:
  - primera puja debe ser al menos precio base;
  
Para subastas `oro` y `platino`:

- No aplican límites de 1% mínimo ni 20% máximo.
- De todos modos, la puja debe superar estrictamente la mejor oferta actual.

Ejemplo base para subastas comun, especial o plata:

- precio base 10.000;
- última oferta 15.000;
- mínima normal 15.100;
- máxima normal 17.000.

- Un usuario puede pujar sin inscripción previa, siempre que cumpla las reglas de cuenta, categoría, medio de pago, moneda y estado de subasta, entre otros. Sin embargo, no puede pujar por un bien consignado por él mismo.

## Concurrencia de pujas

- Al enviar una puja, bloquear/controlar transaccionalmente el estado del lote/subasta.
- No aceptar pujas basadas en una `mejorOferta` vieja.
- Devolver `409 Conflict` si otro usuario ya superó la oferta antes.
- No permitir que el mismo usuario emita otra puja hasta que la anterior esté confirmada/rechazada.
- Registrar todas las pujas en orden real, con timestamp y número de secuencia.
- Emitir estado y mejor oferta por WebSocket a usuarios autenticados
  habilitados para navegar; enviar resultados individuales de puja solo por
  colas privadas.

## Puja ganadora y pago

Cuando termina la puja:

- el último mejor postor pasa a ser comprador del bien;
- se registra venta/compra con medio de pago y datos del usuario;
- el producto se marca como vendido;
- primero se paga unicamente por el monto pujado;
- luego, en el proceso separado del apartado de compras, se pagaran comisiones, envío si aplica;
- se notifica al comprador.

Si nadie puja:

- la empresa compra el artículo por el precio base;
- se necesita modelar un comprador interno de empresa.

## Multas

Si al momento de pagar el usuario no posee fondos o ocurre excepción que impide concretar el pago:

- se genera multa del 10% del valor ofertado;
- el usuario queda en `restriccion_multa`;
- no puede participar en nuevas subastas hasta pagar multa + obligación pendiente;
- debe pagar dentro de 72 horas;
- si no cumple, pasa a `bloqueada_permanente` y el caso se deriva fuera de la app.

## Entrega/retiro y proceso de compra

- El comprador puede elegir:
  - envío: debe pagar envío y comisiones; mantiene cobertura/seguro según política;
  - retiro personal: al retirar pierde cobertura del seguro. Y paga unicamente por comisiones;
- paga por el monto generado segun sus elecciones (comision + envio -si aplica-);
- se pasa al estado de retiro o entrega pendiente segun sea acorde;
- el comprador toma posesion del articulo/producto comprado.

Si no paga envío/comisiones o no retira en plazo, se aplica la regla de que la empresa se queda con el dinero y el artículo, según el flujo original. Modelar esto con estados y auditoría.

## Consignación

Un usuario validado puede consignar bienes sin rol separado. Debe:

- cargar datos del bien;
- subir al menos 6 fotos;
- declarar propiedad legítima y ausencia de impedimentos;
- aceptar que puede requerirse documentación de origen lícito;
- aceptar condiciones de devolución con cargo si el bien no es aceptado.

Para iniciar una consignación debe tener:

- al menos una cuenta bancaria registrada que no esté `rechazada` ni
  `eliminada`, como destino de una eventual liquidación;
- al menos un medio de pago registrado de cualquier tipo que no esté
  `rechazado` ni `eliminado`.

La cuenta bancaria también satisface el segundo requisito. Para iniciar no se
exige que la validación manual de empresa esté vigente. Si luego corresponde
pagar un envío de devolución, el medio elegido en ese momento puede ser otro,
pero debe estar verificado, con `verificado_hasta` vigente, moneda compatible y
fondos o límite suficientes. Elegir retiro no genera pago de envío.

Para liquidar se exige una cuenta bancaria destino registrada y no eliminada,
pero no una verificación manual vigente. Los datos informados son
responsabilidad del usuario. Un rechazo posterior al inicio no bloquea
automáticamente la liquidación.

La tabla central del flujo debe ser `solicitudes_consignacion`, con un registro por artículo/item. No se permite más de un bien por solicitud.

El registro legacy en `productos` solo debe crearse cuando:

- la empresa verificó al consignador como `duenio` si correspondía;
- existe revisor de producto;
- la empresa propuso acuerdo;
- el usuario aceptó el acuerdo.

Antes de eso, todo vive en tablas nuevas.

## `duenios`

- Usar una fila por persona habilitada como consignador, no una fila por bien.
- No crear `duenios` al iniciar consignación si todavía no hay verificador.
- Crear/asegurar duenios obligatoriamente antes de proponer el acuerdo de consignación. No alcanza con validarlo recién antes de crear productos, porque el acuerdo solo puede proponerse cuando la empresa ya verificó al consignador en algún momento previo.
- `verificacionFinanciera`, `verificacionJudicial`, `calificacionRiesgo`: no inventar defaults; cargar manualmente cuando empresa evalúa.
- `verificador`: empleado que validó al consignador.

## Seguros

- Cada bien recibido para venta tiene seguro según valor base.
- El seguro puede combinar varias piezas del mismo dueño.
- El dueño puede ver ubicación del bien y póliza.

## Notificaciones

Generar notificaciones para:

- aprobación/rechazo de registro;
- medio de pago validado/rechazado;
- inscripción a subasta;
- puja superada;
- puja ganadora;
- compra pendiente de pago;
- multa generada/vencida/pagada;
- consignación aprobada/rechazada;
- acuerdo disponible;
- acuerdo aceptado/rechazado;
- devolución/liquidación;
- documentos disponibles.
