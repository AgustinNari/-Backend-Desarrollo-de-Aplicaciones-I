# Material fuente: endpoints_originales.md

> Este archivo conserva el texto original recibido para que Codex tenga acceso a todos los detalles, incluso los que no estén resumidos en la documentación central.

---

Esto seria una definicion general de los endpoints que tendria la app, segun todas las necesidades y demas, pero sin incluir posibles endpoints adicionales para algo tipo “endpoints de admin”, ya que esos endpoints para un “modo admin” serian unicamente de manera auxiliar, ya que no son necesarios para entregar formalmente en si, pero si que podrian agregarse para despues usar en las pruebas, es decir, no son parte de los requisitos en si pedidos, pero si que se pueden agregar por voluntad propia, como para ayudar en las pruebas y demas (por ej, un endpoint para validar un usuario, validar metodos de pago, crear subastas, entre muchas otras posibilidades, pero que todo eso no tendria UI en si en el front de la app ni nada por el estilo). Entonces, tenes libertad para definir esos endpoints y demas de lo relacionado a admin como veas acorde, PERO POR OTRA PARTE, IMPORTANTE, en general, intentar no cambiar, o cambiar lo menos posible de los endpoints que si defini a continuacion (solo cambiar/agregar algo si hace falta de verdad), ya que una de las consignas generales del desarrollo de la app en si y demas, es que el desarrollo y resultado final de la app quede lo mas cercano posible al diseño que se hizo como paso previo, como para aprender buenas practicas de desarrollar en base al diseño y planeacion que se hayan hecho:

Convenciones Generales
En cuanto a los estándares de seguridad y acceso, se establece que todos los puntos de enlace del sistema, a excepción de los módulos dedicados específicamente a la autenticación y el registro inicial, requieren obligatoriamente la inclusión de un token de autorización en el encabezado de cada solicitud. Esta convención se asume como una condición implícita para la interacción con la interfaz, por lo cual, aunque no figure de manera detallada en las tablas de parámetros de cada método individual, representa un requisito fundamental para garantizar la integridad y el control de acceso en todas las operaciones protegidas del servidor.
Por otro lado, la comunicación de salida se rige por un formato de respuesta JSON estandarizado que asegura la consistencia y previsibilidad en el manejo de los datos por parte del cliente. Toda respuesta emitida por la plataforma seguirá una estructura uniforme compuesta por las propiedades data, que contiene el cuerpo de la información solicitada; message, para proporcionar retroalimentación descriptiva sobre el proceso; y una sección opcional de errors, destinada a detallar fallos específicos, facilitando así una integración técnica más clara y eficiente. El estado de la operación se determina a través de los códigos de estado HTTP, los cuales indican si la solicitud fue procesada correctamente o si ocurrió algún error.


1. Autenticación y Registro
 POST    /api/auth/registro/etapa1
Primera etapa del registro: captura los datos personales básicos del postor.

Parámetros
Parámetro
Tipo
email
string
nombre
string
apellido
string
domicilioLegal
string
idPaisOrigen
integer

Retorna
Objeto con idRegistro y el siguiente paso a ejecutar en el flujo de registro.

Respuestas
Código
Estado
Mensaje
201
Created
Datos registrados correctamente
400
Bad Request
Campos obligatorios faltantes
409
Conflict
El email ya está registrado
422
Unprocessable Entity
idPaisOrigen inválido o formato de email incorrecto

 
 POST    /api/auth/registro/etapa2
Segunda etapa: subida de fotos del DNI (frente y dorso). Tras esto la cuenta queda pendiente de revisión manual.

Parámetros
Parámetro
Tipo
email
string
fotoFrenteDni
file
fotoDorsoDni
file

Retorna
Confirmación del upload y estado de la cuenta en revisión.

Respuestas
Código
Estado
Mensaje
200
OK
Fotos recibidas. Cuenta en revisión
400
Bad Request
Falta alguna foto o formato inválido
404
Not Found
Email no corresponde a una etapa 1 iniciada
409
Conflict
Las fotos ya fueron subidas previamente
422
Unprocessable Entity
Tamaño o formato de imagen no soportado

 
 POST    /api/auth/registro/verificar-token
Valida el token del enlace enviado al mail tras la aprobación de la cuenta por parte de la empresa.

Parámetros
Parámetro
Tipo
token
string

Retorna
Acceso necesario para completar la etapa de creación de clave.

Respuestas
Código
Estado
Mensaje
200
OK
Enlace válido
400
Bad Request
Enlace inválido
404
Not Found
Sin código pendiente
410
Gone
El token expiró

 
 POST    /api/auth/registro/etapa3
Etapa final: establece la clave personal del usuario y finaliza el registro.

Parámetros
Parámetro
Tipo
setup_token
string
clave
string
claveConfirmacion
string

Retorna
Token de sesión y datos básicos del usuario.

Respuestas
Código
Estado
Mensaje
200
OK
Registro finalizado exitosamente
400
Bad Request
Clave no cumple requisitos o no coincide con confirmación
401
Unauthorized
Token temporal inválido o expirado

 POST    /api/auth/registro/reenviar-link
Se ingresa el mail para poder recibir nuevamente un correo con un nuevo enlace para compleción de registro (si aplica).

Parámetros
Parámetro
Tipo
email
string

Retorna
Envío de mail (si aplica)

Respuestas
Código
Estado
Mensaje
200
OK
Si el email es válido, se enviará un enlace para continuar el registro
400
Bad Request
Email inválido
429
Too Many Requests
Demasiados intentos, intente más tarde

 POST    /api/auth/login
Inicia sesión con email y clave. Retorna token y estado de cuenta.

Parámetros
Parámetro
Tipo
email
string
clave
string

Retorna
Token y datos del usuario

Respuestas
Código
Estado
Mensaje
200
OK
Login exitoso
401
Unauthorized
Email o clave incorrectos
403
Forbidden
Cuenta bloqueada o pendiente de aprobación
404
Not Found
Email no registrado

 
 POST    /api/auth/recuperar-clave
Solicita envío de enlace de recuperación al mail registrado.

Parámetros
Parámetro
Tipo
email
string

Retorna
Confirmación del envío del mail de recuperación.

Respuestas
Código
Estado
Mensaje
200
OK
Si el email existe se envía mail
429
Too Many Requests
Más de 3 intentos en 15 minutos

 
 PUT    /api/auth/cambiar-clave
Establece nueva contraseña (desde recuperación o desde sesión activa en perfil).

Parámetros
Parámetro
Tipo
claveNueva
string
claveConfirmacion
string
token
string

Retorna
Confirmación del cambio de contraseña.

Respuestas
Código
Estado
Mensaje
200
OK
Contraseña actualizada exitosamente
400
Bad Request
Clave no cumple requisitos o no coincide la confirmación
401
Unauthorized
Token inválido/expirado

2. Métodos de Pago
 
 GET    /api/usuario/medios-pago
Lista los medios de pago registrados por el usuario autenticado.

Retorna
Array de medios con datos

Respuestas
Código
Estado
Mensaje
200
OK
Lista de medios de pago
401
Unauthorized
Token inválido

 
 
 POST    /api/usuario/medios-pago
Registra un nuevo medio de pago (tarjeta, cuenta bancaria o cheque). Queda pendiente de verificación manual.

Parámetros
Parámetro
Tipo
tipo
string
moneda
string
nombreTitular (Tarjeta)
string
numeroTarjeta (Tarjeta)
string
vencimiento (Tarjeta)
string
cvv (Tarjeta)
string
titular (Tarjeta)
string
numeroCuenta (Cuenta)
string
nombreBanco
string
alias (Cuenta)
string
nacional  (Tarjeta y Cuenta)
boolean
monto (Cheque)
decimal
fechaVencimiento (Cheque)
date
numeroCheque (Cheque)
string
fotoAnverso (Cheque)
file
fotoReverso (Cheque)
file

Retorna
ID del medio de pago creado y estado inicial 'pendiente_verificacion'.

Respuestas
Código
Estado
Mensaje
201
Created
Medio registrado. Pendiente de verificación
400
Bad Request
Campos faltantes según tipo o CVV inválido
401
Unauthorized
Token inválido
409
Conflict
Ya existe un medio idéntico registrado
422
Unprocessable Entity
Foto de cheque con formato no soportado

 
 
 DELETE    /api/usuario/medios-pago/{id}
Elimina lógicamente un medio de pago. .

Parámetros
Parámetro
Tipo
id
integer

Retorna
Confirmación de la eliminación lógica.

Respuestas
Código
Estado
Mensaje
200
OK
Medio de pago eliminado correctamente.
403
Forbidden
El medio de pago no pertenece al usuario.
404
Not Found
Medio de pago no encontrado.
409
Conflict
No es posible eliminar el medio de pago porque se encuentra asociado a una operación pendiente.

 PATCH    /api/usuario/medios-pago/{id}/principal
Marca un método de pago como principal. Desactiva el anterior si es que había uno

Parámetros
Parámetro
Tipo
id 
string

Retorna
Confirmación de la actualización.

Respuestas
Código
Estado
Mensaje
200
OK
Medio de pago establecido como principal exitosamente.
404
Not Found
Medio de pago no encontrado.

3. Perfil, Notificaciones, Ayuda y Estadísticas
 
 GET    /api/usuario/perfil
Obtiene los datos personales del usuario autenticado y su categoría actual.

Retorna
Email, nombre, dirección, categoría, entre otros

Respuestas
Código
Estado
Mensaje
200
OK
Perfil del usuario
401
Unauthorized
Token inválido

 
 
 GET    /api/usuario/estadisticas
Retorna métricas del usuario con filtro temporal (mes, trimestre o anual).

Parámetros
Parámetro
Tipo
periodo
string

Retorna
Total pujado, porcentaje de éxito, total pagado

Respuestas
Código
Estado
Mensaje
200
OK
Métricas calculadas
401
Unauthorized
Token inválido

 GET    /api/usuario/historial
Historial de pujas del usuario.

Parámetros
Parámetro
Tipo
page
integer
limit
integer

Retorna
Lista paginada de ítems, subastas y montos.

Respuestas
Código
Estado
Mensaje
200
OK
Historial paginado
401
Unauthorized
Token inválido

 
 GET    /api/usuario/notificaciones
Lista las notificaciones: Todo / Subastas / Transacciones.

Parámetros
Parámetro
Tipo
categoria
string
leidas
boolean
page
integer

Retorna
Lista de notificaciones con tipo, mensaje, fecha y contador global de no leídas.

Respuestas
Código
Estado
Mensaje
200
OK
Lista de notificaciones
401
Unauthorized
Token inválido

 

 PATCH    /api/usuario/notificaciones/{id}/leer
Marca una notificación como leída. Se puede pasar 'all' para marcar todas.

Parámetros
Parámetro
Tipo
id (o “all”)
integer | string 

Retorna
Confirmación de la actualización.

Respuestas
Código
Estado
Mensaje
200
OK
Notificación marcada como leída
403
Forbidden
La notificación no pertenece al usuario
404
Not Found
Notificación no encontrada

4. Subastas y Pujas
 
 GET    /api/subastas
Listado de subastas disponibles (activas y próximas).

Parámetros
Parámetro
Tipo
estado
string
segmento
string
categoria
string
moneda
string
page
integer

Retorna
Array de subastas con título, estado, ubicación, categoría, segmento, moneda, entre otros.

Respuestas
Código
Estado
Mensaje
200
OK
Lista de subastas
400
Bad Request
Algún parámetro de consulta tiene un valor inválido.
422
Unprocessable Entity
Formato incorrecto en los filtros enviados.

 GET    /api/subastas/{id}
Detalle de una subasta específica con información de inscripción.

Parámetros
Parámetro
Tipo
id
integer

Retorna
Datos completos de la subasta junto con condiciones de aceptabilidad para poder participar.

Respuestas
Código
Estado
Mensaje
200
OK
Detalle de la subasta
404
Not Found
Subasta no encontrada

 

 GET    /api/subastas/{id}/catalogo
Catálogo de ítems de una subasta. Para usuarios invitados omite precios y pujas.

Parámetros
Parámetro
Tipo
id
integer
page
integer

Retorna
Lista de ítems con lote, precio base, título, imagen principal, estado, entre otros.

Respuestas
Código
Estado
Mensaje
200
OK
Catálogo de la subasta
404
Not Found
Subasta no encontrada

 GET    /api/items/{id}
Detalle completo de un ítem: como fotos, autor, historia y sub-ítems.

Parámetros
Parámetro
Tipo
id
integer

Retorna
Datos completos del ítem junto con galería de fotos.

Respuestas
Código
Estado
Mensaje
200
OK
Detalle del ítem
404
Not Found
Ítem no encontrado

 
 POST    /api/subastas/{id}/inscribirse
Pre-inscripción a una subasta futura (hasta 30 min antes del inicio). Selecciona el medio de pago a verificar.

Parámetros
Parámetro
Tipo
id
integer
idMedioPago
integer

Retorna
Confirmación de inscripción y detalles.

Respuestas
Código
Estado
Mensaje
201
Created
Inscripción enviada
400
Bad Request
La subasta ya comenzó (menos de 30 min)
403
Forbidden
Categoría insuficiente o multa activa
404
Not Found
Subasta o medio de pago no encontrado
409
Conflict
Ya está inscripto a esta subasta
422
Unprocessable Entity
La moneda del medio de pago no coincide con la subasta

 
 POST    /api/subastas/{id}/verificacion
Realiza las verificaciones y comprobaciones necesarias para determinar si el usuario está capacitado para realizar pujas/participar de manera acorde en una subasta o no.

Parámetros
Parámetro
Tipo
id
integer

Retorna
Número de postor, registra asistencia al momento de pujar.

Respuestas
Código
Estado
Mensaje
200
OK
Acceso concedido
403
Forbidden
Sin medio verificado con moneda acorde, multa activa o categoría insuficiente
404
Not Found
Subasta no existe o no está activa
409
Conflict
Ya está conectado a otra subasta en vivo

 
 GET    /api/subastas/{id}/puja-actual
Ítem siendo subastado y mejor oferta actual 

Parámetros
Parámetro
Tipo
id
integer

Retorna
Ítem actual, mejor oferta (contador de tiempo)  y número de postor ganador (anónimo), junto con historial reciente y detalles de ítem.

Respuestas
Código
Estado
Mensaje
200
OK
Puja actual
404
Not Found
Subasta finalizada o sin ítem activo

 
 
 POST    /api/subastas/{id}/pujar
Registra una nueva oferta sobre el ítem actual. Valida límites de categoría y fondos del medio de pago.

Parámetros
Parámetro
Tipo
id
integer
idItem
integer
valorOfertado
decimal
idMedioPago
integer

Retorna
ID de la puja, valor ofertado, flag de puja ganadora y siguiente monto mínimo.

Respuestas
Código
Estado
Mensaje
201
Created
Puja registrada
400
Bad Request
MONTO_MENOR_MINIMO | MONTO_MAYOR_MAXIMO | MONTO_EXCEDE_LIMITE_CATEGORIA | MONTO_EXCEDE_LIMITE_MEDIO_PAGO
403
Forbidden
MULTA_ACTIVA | PUJA_ACTIVA_OTRA_SUBASTA | CATEGORIA_INSUFICIENTE
409
Conflict
ITEM_SUBASTADO (otro postor ya ganó)
422
Unprocessable Entity
MEDIO_NO_VERIFICADO o sin fondos

 
5. Mis Compras
 
 GET    /api/compras
Lista las compras del usuario con tabs:  pendiente,  pagada,  con multa, con envío pendiente, entre otros.

Parámetros
Parámetro
Tipo
estado
string

Retorna
Lista paginada con imagen, nombre, monto, estado, acción requerida, entre otros.

Respuestas
Código
Estado
Mensaje
200
OK
Lista de compras
401
Unauthorized
Token inválido

 
 
 GET    /api/compras/{id}
Detalle completo de una compra con desglose de costos y modalidad de entrega.

Parámetros
Parámetro
Tipo
id
integer

Retorna
Desglose (oferta, comisión, envío, total), datos de entrega, multa si aplica, entre otros.

Respuestas
Código
Estado
Mensaje
200
OK
Detalle de la compra
403
Forbidden
La compra no pertenece al usuario
404
Not Found
Compra no encontrada

 

 PUT    /api/compras/{id}/entrega
Define la modalidad de entrega: retiro en sucursal o envío a domicilio.

Parámetros
Parámetro
Tipo
id
integer
metodoEntrega
string
direccionEnvio
string
piso
string
codigoPostal
string
localidad
string
provincia
string
telefonoContacto
string

Retorna
Nuevo desglose de costos con envío.

Respuestas
Código
Estado
Mensaje
200
OK
Modalidad guardada
400
Bad Request
Falta dirección para envío
403
Forbidden
La compra no pertenece al usuario
409
Conflict
La compra ya fue pagada

 
 
 POST    /api/compras/{id}/pagar
Procesa el pago de comisiones + envío (si aplica). Genera factura de adjudicación.

Parámetros
Parámetro
Tipo
id
integer
idMedioPago
integer

Retorna
Datos totales del resumen de compra.

Respuestas
Código
Estado
Mensaje
200
OK
Pago confirmado
400
Bad Request
Modalidad de entrega no definida
403
Forbidden
La compra no pertenece al usuario
409
Conflict
Ya fue pagada
422
Unprocessable Entity
Fondos insuficientes

 
 
 POST    /api/compras/{id}/pagar-con-multa
Pago que regulariza el estado de una compra efectuada en la app (Pago conjunto de ítem + multa)

Parámetros
Parámetro
Tipo
id
integer
idMedioPago
integer

Retorna
Datos totales del resumen de pago de ítem por valor pujado + multa.

Respuestas
Código
Estado
Mensaje
200
OK
Ítem y multa pagados
400
Bad Request
El nuevo valor total ya está pagado
403
Forbidden
El ítem o la multa no pertenecen al usuario
404
Not Found
Ítem o multa no encontradas
422
Unprocessable Entity
Fondos insuficientes

 
 
 GET    /api/compras/{id}/documentos
Lista los documentos de la compra: Factura de Adjudicación y Recibo de Multa si aplica.

Parámetros
Parámetro
Tipo
id
integer

Retorna
Documentos disponibles con tipo, número, estado y los detalles particulares de cada uno.

Respuestas
Código
Estado
Mensaje
200
OK
Lista de documentos
403
Forbidden
La compra no pertenece al usuario



6. Consignación de Bienes
 
 GET    /api/consignaciones/requisitos
Valida si el usuario cumple los requisitos para consignar (medio de pago + cuenta de cobro).

Retorna
Flag 'puedeContinuar' y lista de requisitos con estado de cumplimiento.

Respuestas
Código
Estado
Mensaje
200
OK
Estado de requisitos
401
Unauthorized
Token inválido

 
 
 POST    /api/consignaciones
Crea una nueva consignación con datos del bien y mínimo 6 fotos. Requiere declaración jurada de propiedad y origen lícito.

Parámetros
Parámetro
Tipo
segmento
string
aceptaTyC
boolean
declaracionPropiedadYOrigenLicito
boolean
titulo
string
descripcion
string
historia
string
fechaAproximada
string
esObraDeArte
boolean
autor (si es obra de arte)
string
historiaExtendida (si es obra de arte)
string
fotos
file[]

Retorna
Detalles de la solicitud y próximos pasos.

Respuestas
Código
Estado
Mensaje
201
Created
Consignación creada
400
Bad Request
Menos de 6 fotos o TyC/declaración no aceptados
403
Forbidden
Requisitos previos no cumplidos
422
Unprocessable Entity
Formato de imagen inválido

 
 
 POST    /api/consignaciones/{id}/documentacion-origen
Sube documentación para acreditar origen lícito (inicial o cuando la empresa la solicita).

Parámetros
Parámetro
Tipo
id
integer
facturaCompra
file
certificadoAutenticidad
file
observaciones
string

Retorna
Confirmación del envío. La consignación vuelve a estado 'pendiente_revision'.

Respuestas
Código
Estado
Mensaje
200
OK
Documentación enviada
400
Bad Request
Ningún archivo adjunto
403
Forbidden
La consignación no pertenece al usuario
409
Conflict
La consignación no acepta documentación en su estado actual

 
 
 GET    /api/consignaciones
Lista las consignaciones del usuario con tabs: Activas / Rechazadas / Vendidas.

Parámetros
Parámetro
Tipo
filtro
string
page
integer

Retorna
Lista de consignaciones con código, imagen principal, título, estado, valor, acción pendiente, entre otros.

Respuestas
Código
Estado
Mensaje
200
OK
Lista de consignaciones
401
Unauthorized
Token inválido

 
 
 GET    /api/consignaciones/{id}
Detalle completo de una consignación con timeline de 5 hitos y documentos disponibles (si aplica).

Parámetros
Parámetro
Tipo
id
integer

Retorna
Detalles de consignación, como código, badges, precio base, comisión, neto estimado, timeline, acciones disponibles y documentos (todo si aplica).

Respuestas
Código
Estado
Mensaje
200
OK
Detalle de la consignación
403
Forbidden
La consignación no pertenece al usuario
404
Not Found
Consignación no encontrada

 
 
 POST    /api/consignaciones/{id}/acuerdo/aceptar
Aceptación digital del acuerdo de consignación (Al confirmar esto, la empresa de subastas luego, con sus plazos, le asignará póliza y subasta al ítem subido).

Parámetros
Parámetro
Tipo
id
integer
leyoContrato
boolean
aceptaClausulasPlazos
boolean

Retorna
Confirmación del acuerdo y datos del documento generado.

Respuestas
Código
Estado
Mensaje
200
OK
Acuerdo aceptado
400
Bad Request
Algún checkbox no aceptado
403
Forbidden
No está en estado acuerdo_pendiente
409
Conflict
El acuerdo ya fue procesado

 

 POST    /api/consignaciones/{id}/acuerdo/rechazar
Rechaza el acuerdo propuesto. Redirige al flujo de devolución del bien.

Parámetros
Parámetro
Tipo
id
integer

Retorna
Confirmación del rechazo. Pasa a estado 'devolucion_pendiente'.

Respuestas
Código
Estado
Mensaje
200
OK
Rechazo registrado
409
Conflict
El acuerdo ya fue procesado

 

 POST    /api/consignaciones/{id}/devolucion
Elige modalidad de devolución de un bien rechazado.

Parámetros
Parámetro
Tipo
id
integer
modalidad
string
direccion
string
piso
string
codigoPostal
string
localidad
string
provincia
string
telefonoContacto
string

Retorna
Confirmación con costo de envío calculado si aplica.

Respuestas
Código
Estado
Mensaje
201
Created
Devolución registrada
400
Bad Request
Falta dirección o modalidad inválida
409
Conflict
Ya gestionada o plazo vencido

 POST    /api/consignaciones/{id}/devolucion/pagar-envio
Paga el costo del envío de devolución.

Parámetros
Parámetro
Tipo
id
integer
idMedioPago
integer

Retorna
Detalles de pago de envío.

Respuestas
Código
Estado
Mensaje
200
OK
Pago procesado
409
Conflict
Ya fue pagado
422
Unprocessable Entity
Fondos insuficientes

