# Material fuente: consideraciones_backend_extensas_original.md

> Este archivo conserva el texto original recibido para que Codex tenga acceso a todos los detalles, incluso los que no estén resumidos en la documentación central.

---

CONSIDERACIÓN GENERAL Y MUY IMPORTANTE N1: Todo lo que haga referencia a nuevos atributos, nuevos valores posibles de atributos, o extensiones generales del funcionamiento de cualquier entidad o aspecto de la app, deberá ser tratado con la creación de nuevas tablas o estructuras. Esto se debe a que no se pueden agregar atributos, valores posibles de atributos ni modificar ningún aspecto de las tablas del sistema ya existente (no modificar nada ni agregar nada de la estructura SQL del mismo).

CONSIDERACIÓN GENERAL Y MUY IMPORTANTE N2:
Todo lo que se detalla y aclara a continuación esta sujeto a ciertas modificaciones y/o ampliaciones posibles si se considera acorde y si mejoraria el funcionamiento general de la app. Adicionalmente, cabe destacar que todo lo incluido y explicado a continuación no se supone que cubre toda la estructura general del negocio, ni tampoco todas las tablas y estructuras nuevas que haga falta crear, ni todas las consideraciones generales de como armar backend; sino que serían simplemente algunos detalles y explicaciones/aclaraciones puntuales que me parecieron útiles destacar y tener en cuenta al armar el backend y todo lo relacionado.
1) Alta de usuario: solicitudes_registro + personas + clientes
Yo lo haría en dos capas:
Antes de aprobación
Crear una tabla nueva, por ejemplo:
•	solicitudes_registro
Ahí guardaría todo lo que todavía no entra en las tablas heredadas:
•	email
•	nombre
•	apellido
•	domicilio legal
•	país de origen
•	fotos DNI frente/dorso
•	estado del proceso
•	tokens temporales
•	fechas de expiración
Cuando la empresa aprueba
En una sola transacción:
•	se crea personas
•	se crea clientes
•	se copia el país de origen a clientes.numeroPais
•	se marca la solicitud como aprobada
Recomendación concreta
•	personas y clientes se crean juntas solo al aprobar
•	no antes
•	el país queda en la tabla temporal hasta la aprobación
Vigencia recomendada del registro pendiente
•	Expiración de la solicitud de registro: 24 horas desde la última actividad
•	Token para completar el registro final: 48 horas y un solo uso
CONSIDERACIÓN GENERAL IMPORTANTE PARA TODO: OBVIAMENTE, COMO PASA EN ESTE CASO Y EN VARIOS OTROS DEL FUNCIONAMIENTO GENERAL DEL NEGOCIO, HACE FALTA AGREGAR TABLAS ADICIONALES MÁS ALLÁ DE LO RECIÉN MENCIONADO. Por ejemplo, para este caso, las tablas de persona y clientes no sirven para almacenar cosas como email de login, hash de contraseña, refresh token, último acceso, estado de sesión, intentos de login, recuperación de clave, entre otros; por eso es que se deberia crear tambien una tabla pero de guardado permanente (cuentas_app por ej.) para que registre dichos datos y complemente a las tablas de personas y clientes. Tambien cabe considerar que esto pasa en varios aspectos a lo largo de la app, pero no son mencionados tan explicitamente a lo largo del documento ya que se considera algo relativamente obvio; sino que los aspectos mencionados a lo largo de este documento serian mas que nada relacionados a los apartados que puedan generar mas conflictos o dudas al armar el backend y el funcionamiento general de la app.

________________________________________
2) Tabla duenios
Sí, yo la trataría como la tabla de usuarios habilitados para consignar bienes, pero no como “una fila por bien”.
Uso recomendado
•	1 fila por persona que entra al circuito de consignación
•	se reutiliza para todos los bienes de ese usuario
Campos extra (verificaciónFinanciera, verificaciónJudicial, calificacionRiesgo)
Mi recomendación es:
•	no inventar defaults
•	no usarlos como valores fijos de arranque
•	cargarles valor solo cuando la empresa evalúa al consignador
Cómo tratarlos
•	verificaciónFinanciera: NULL hasta revisión
•	verificaciónJudicial: NULL hasta revisión
•	calificacionRiesgo: NULL hasta revisión
•	verificador: empleado que hizo la revisión
Importante
Como verificador es NOT NULL, mi recomendación es:
•	no crear duenios hasta que ya exista un empleado asignado a la evaluación
O sea, igual que con clientes:
•	primero una tabla de solicitud/flujo temporal,
•	después el alta definitiva en duenios.
Escala recomendada para calificacionRiesgo
Usaría exactamente la escala de la tabla, pero con semántica clara:
•	1 = riesgo muy bajo
•	2 = bajo
•	3 = medio-bajo
•	4 = medio
•	5 = alto
•	6 = muy alto
Regla práctica recomendada
•	1 a 4: consignación normal
•	5: revisión extra
•	6: rechazo por defecto salvo override manual
- CONSIDERACIÓN IMPORTANTE: Esta validación y asignación de estos atributos a un usuario que suba un artículo para consignar, no lo debería limitar de ninguna manera en el flujo de consignación en sí.
Esto se debe a que una vez que tiene una cuenta validada de forma inicial luego del registro, con creación de contraseña y demás, luego podrá simplemente iniciar el proceso de consignación, subir el artículo para validación, y ya en ese momento se deberá crear un registro en una tabla tipo solicitudes_consignacion.
Esto ultimo se debe a que para crear un registro en la tabla productos, se necesita una correspondencia en la tabla duenios, y para crear un registro en la tabla duenios se necesita una correspondencia en la tabla de empleados, al tener ese atributo de verificador, pero si todavia no verificaron nada, entonces no se sabria que asignar como verificador de ese nuevo registro de duenios, por lo tanto, no se podria crear ese nuevo registro.
Entonces, el flujo seria asi: En cualquier nueva consignacion iniciada, se crea un registro en esa nueva tabla de solicitudes_consignacion, se sigue el proceso normal de flujo y estados de esa consignacion, y si se valida todo, sin rechazar por alguna razon en el medio, entonces se quedara en el paso de revision fisica; pero no se permitira a ningun empleado de la empresa proponer el acuerdo de consignacion y pasar a su estado acorde sin que antes, en algun momento, se hayan realizado las verificaciones y asignaciones de valores a atributos necesarias para un registro de la tabla de duenios (realizado también de manera manual por algun empleado de la empresa, pero no necesariamente el mismo que actúa en el proceso de consignación del bien especifico. Ademas, utilizando el id del empleado que realice dichas verificaciones del usuario vendedor, para atributo verificador en el registro acorde de la tabla duenios). Luego, si ya tiene el registro acorde en la tabla de duenios (1 unico registro posible en dicha tabla por cada usuario), entonces algun empleado de la empresa puede realizar el acuerdo de consignacion y se le permite al usuario aceptarlo o rechazarlo. En ese momento, unicamente si el usuario acepta el acuerdo, entonces recien ahi se crea el registro acorde en la tabla de productos, con las asignaciones acordes de los atributos y utilizando el id del empleado que haya actuado en el proceso de consignación para el campo de revisor en ese registro específico (Esta información para rellenar los campos, el revisor y demas, seria tomada del registro acorde que ya deberia haber sido creado al inicio de todo el proceso, en la nueva tabla de solicitudes_consignacion).
- Regla general para la tabla solicitudes_consignacion:
Se maneja con 1 articulo/item/producto por cada registro en esta tabla y viceversa. Sin posibilidad de hacer mas de 1 item por cada registro/fila.
Luego, cabe destacar la importancia de todo el flujo descrito y la necesidad de que el usuario no vea reducidas sus capacidades de consignacion y de uso general de la app por la necesidad de esa verificacion antes de la creacion de un registro en la tabla duenios. SIno que simplemente lo maneja la empresa de manera manual e idealmente no afecta negativamente al usuario.
En general, esto se podria pensar de manera similar a como funcionaria la tabla solicitudes_registro que usamos para el registro de usuarios. Solo que aqui, la tabla solicitudes_consignacion se utilizaria para todo el todo el proceso de cosnginación y lo siguiente de cada producto/articulo subido. Es decir, la tabla solicitudes_consignacion en general acompañaría todo el proceso de consignación hasta la liquidación.


- Consideración adicional:
Separaría verificador de revisor
•	verificador = empleado que valida a la persona o al consignador.
•	revisor = empleado que revisa el producto/ítem concreto.
No es lo mismo validar a la persona que aprobar el artículo en sí. Porque aunque pueda hacerlo el mismo empleado, no necesariamente será siempre así.
Adicionalmente, cabe destacar que la tabla solicitudes_consignacion seria de cierta forma la tabla central del proceso, como un registro maestro del flujo completo de cada articulo consignado, desde que el usuario lo carga hasta que termina el proceso, incluyendo:
•	carga inicial,
•	revisión digital,
•	pedido de documentación,
•	revisión física,
•	acuerdo,
•	publicación,
•	subasta,
•	venta,
•	liquidación,
•	devolución si aplica.
Sobre una FK a productos
La tabla solicitudes_consignacion seguramente podria tener FK a productos, nullable al principio.
Yo lo haría así:
•	al crear la consignación: idProducto = NULL
•	cuando ya se aprueba, el usuario acepta el acuerdo y por ende se genera el producto real: se completa idProducto
•	si el flujo termina rechazado o devuelto, entonces debería quedar siempre sin producto en el atributo idProducto de la tabla solicitudes_consignacion, ya que el registro en la tabla productos se crearía únicamente al momento de aceptar el acuerdo de consignacion. Ademas, cabe recalcar que desde ese punto, es decir, cuando el usuario acepta el acuerdo de cosnginacion, ya no se podria generar ningun rechazo ni devolucion del artículo/producto a consignar.
Pero haría una precisión para la tabla solicitudes_consignacion
Posiblemente no usar solo idProducto como vínculo principal.
La tabla debería seguramente al menos tener también:
•	idPersona o idCliente del consignador,
•	idProducto nullable,
•	idItemCatalogo nullable, agregado si ya fue asignado a una subasta,
•	idSubasta nullable, agregado si ya está publicada,
•	y otros campos de estado separados.
Sobre duenios como validación interna de consignadores
Yo no haría que el alta de consignación dependa de que duenios exista desde el primer momento.
Más bien:
•	el usuario puede iniciar la consignación sin problema,
•	queda en solicitudes_consignacion,
•	pero el backend no permite pasar a la fase de acuerdo final (proponer el acuerdo de cosnginacion) hasta que la empresa:
o	valide al usuario como dueño/consignador,
o	y cree el registro en la tabla duenios.
Pero eso si, cabe destacar lo siguiente:
No permitiria que el registro en la tabla productos se cree sin antes tener claro el duenio
Como productos.duenio es NOT NULL, entonces en el momento de crear el registro en la tabla productos ya se debe tener:
•	duenio creado,
•	revisor definido (guardado primero en la tabla solicitudes_consignacion para poder despues agregarlo al registro acorde de la tabla productos)
•	y el artículo con acuerdo de consignacion aceptado para pasar al circuito definitivo.

________________________________________
3) Qué significa verificador
Yo lo dejaría como:
•	FK a empleados
•	representa al empleado que validó o aprobó ese registro
En clientes
Es el empleado que validó al usuario como participante de la app.
En duenios
Es el empleado que evaluó al consignador.
Valor de diseño
Muy útil para:
•	auditoría
•	trazabilidad
•	historial interno
•	soporte
•	pruebas administrativas
________________________________________
4) Estados recomendados de la cuenta
Yo no usaría demasiados. Me quedaría con estos:
En registro temporal (Tabla nueva recomendada: solicitudes_registro)
•	pendiente_revision
•	rechazado
•	aprobada_pendiente_finalizacion (Para cuando la empresa aprueba al usuario pero todavia debe completar el registro creando su contraseña/clave)
•	completada
Uso:
•	cubre el alta previa a personas y clientes
En cuenta activa (Tabla nueva recomendada: cuentas_app)
•	activa
•	restriccion_multa
•	bloqueada_permanente
•	deshabilitada_admin
Uso:
•	representa el acceso operativo real a la app
Notas:
•	el estado de registro pendiente no va acá
•	eso vive en solicitudes_registro
Posiblemente cuentas_app debería colgar/depender también de clientes, y no solo de personas
Se podría hacer FK a clientes, porque la cuenta operativa existe cuando el usuario ya fue aprobado como cliente.
Esto para evitar tener una cuenta “activa” asociada a alguien que todavía no terminó el alta.
Recomendación de lógica
•	la cuenta no existe como operativa hasta la aprobación
•	el login y funcionamiento normal solo funciona si la cuenta está en activa 
•	restriccion_multa hace que un usuario no se puedan hacer nuevas pujas ni inscribirse a una subasta, pero por el resto de funciones y apartados sigue pudiendo hacer literalmente todo (Este estado estará vigente hasta que pague las multas pendientes junto con valor pujado en el mismo pago. O si alguna multa se vence, entonces pasa a estado bloqueada_permanente)
•	bloqueada_permanente corta todo acceso, solo se permitiria entrar a una pantalla de estado, sin navegación real.
________________________________________
5) Categorías de usuario y puntos
Acá sí conviene dejar una escala clara.
Rangos recomendados
Categoría	Puntos
común	0 – 249
especial	250 – 699
plata	700 – 1499
oro	1500 – 2999
platino	3000+
Reglas
•	la categoría cambia automáticamente según puntos
•	la asignación inicial la hace la empresa
•	los puntos nunca bajan de 0
•	si un usuario cruza un umbral, la categoría cambia en el momento
Recomendación de implementación
•	guardar el total acumulado
•	guardar el historial de movimientos
•	recalcular categoría en cada evento importante
•	correr además un job nocturno de reconciliación por seguridad
________________________________________
6) Puntos por acción
Te propongo estos valores:
Acción	Puntos
cada medio de pago verificado	+30
participar en una puja válida	+1 por cada puja realizada
ganar una puja	+80
completar pago de compra a tiempo	+60
consignación aceptada por la empresa y puesto en subasta finalmente	+70
pagar comisiones/envío dentro de plazo	+20
generar multa	-90
multa vencida sin pago y por ende bloqueo permanente de cuenta	-250
Recomendación extra
Mantendría una tabla de:
•	movimientos_puntos
•	con fecha
•	motivo
•	referencia al evento
•	puntos positivos o negativos
________________________________________
7) Medios de pago
Yo dejaría esto bien cerrado:
Tipos soportados
•	tarjeta
•	cuenta bancaria
•	cheque certificado
Estados del medio
Estado	Uso
pendiente	cargado, no usable
verificado	usable
rechazado	no usable
vencido	verificación caducada
eliminado	baja lógica
Obviamente, también se deberían guardar y tener en cuenta atributos como limite_aprobado, consumo_actual, verificado_hasta, moneda, principal, entre otros. Pero guardados en nuevas tablas al no poder modificar lo ya existente.
Recomendación clave
•	no permitir edición
•	solo:
o	crear
o	eliminar lógicamente
o	marcar principal
Verificación
•	siempre manual
•	la empresa decide si aprueba o rechaza
Vigencia recomendada de la verificación
•	5 días hábiles
•	si se revalida, el plazo se reinicia completo
Card payment
•	vigencia técnica de uso: 5 días hábiles
•	límite de consumo: mensual
Bank account
•	vigencia técnica de uso: 5 días hábiles
•	límite de consumo: rolling 30 days o ciclo manual equivalente
Cheque certificado
•	uso vigente hasta agotar el monto garantizado
•	sin reinicio mensual
- Los límites de gasto de cada método de pago se agregaría por parte de la empresa al hacer el proceso de validación, para ver y establecer correctamente el limite restante al momento de realizar cada validación manual.
•	la empresa carga el límite aprobado al momento de la validación
•	el sistema lleva el consumo acumulado
•	cuando alcanza el límite, el medio deja de ser usable hasta la próxima revisión

Medio principal
•	solo uno principal por moneda (Entonces como máximo, 2 principales, 1 para USD y 1 para ARS)
•	sirve como sugerencia por defecto
•	no obliga al backend a usarlo si la subasta requiere otra moneda
________________________________________
8) País de origen y direcciones
Como personas no guarda país, yo haría esto:
País de origen
•	se guarda en solicitudes_registro
•	luego se copia a clientes.numeroPais
•	si ese usuario pasa a consignador, también a duenios.numeroPais
Dirección legal
•	puede quedar en personas.direccion al aprobarse
Direcciones de envío
Yo sí crearía una tabla nueva:
•	direcciones_envio
Recomendación
•	permitir hasta 5 direcciones de envío activas
•	solo 1 principal
•	campos: alias, destinatario, calle, número, piso, CP, localidad, provincia, país, teléfono, entre otros
________________________________________
9) Subastas, catálogo y acceso
Qué ve un invitado
Yo dejaría esto fijo:
•	ve listado de subastas
•	ve detalle general de la subasta
•	ve catálogo e ítems
•	no ve precios
•	no ve pujas
•	no ve estado en vivo
•	no ve historial de ofertas
•	no puede pujar
Qué ve un usuario registrado sin medio verificado
•	puede entrar al detalle
•	puede ver catálogo y subasta
•	puede leer todo
•	no puede pujar
Recomendación técnica
Que el backend devuelva DTOs distintos:
•	DTO público
•	DTO usuario autenticado
No dejar eso solo al frontend.
________________________________________
10) Reglas de puja
Reglas base
•	mínimo incremento: 1% del precio base
•	máximo incremento: 20% del precio base
•	si nadie pujó todavía, la primera puja puede ser igual al precio base
Subastas oro y platino
•	sin mínimo ni máximo
•	solo debe superar la mejor puja anterior por 1 unidad de la moneda
Valores concretos
Si el precio base es 10.000 y la última mejor puja es de 15.000:
•	mínimo: 15.100
•	máximo: 17.000
Si la subasta es oro/platino:
•	puede pujar 15.001 o más, sin tope
Y si no hubiera ninguna puja previa (suponemos que no esta la de 15.000 ni ninguna anterior), entonces podría pujar por el precio base como puja de inicio: 10.000
Retención de puja ganadora
•	mantener la puja viva durante 60 segundos
•	si nadie la supera, el ítem se cierra y pasa al siguiente
Concurrencia
•	una sola puja aceptada por vez
•	idempotency_key por envío de puja
•	lock transaccional por item activo
Timeouts posibles como ejemplo (Se pueden ajustar/modificar si mejoraría el funcionamiento)
•	confirmación websocket: 10 segundos
•	reintento automático de cliente: 1 sola vez
•	heartbeat websocket: cada 30 segundos
________________________________________
11) Compra, multa y pagos
Flujo recomendado
Cuando gana una puja:
1.	se adjudica el ítem
2.	se intenta el cobro inmediato del valor pujando
3.	si falla, se genera multa automática
4.	el usuario luego paga:
o	multa + artículo
o	comisiones + envío si corresponde
Multa
•	valor: 10% del valor ofertado
•	vencimiento: 72 horas
•	si no paga: bloqueo total
Estados de compra recomendados
Estado	Significado
adjudicacion_pendiente	ganó pero aún no se terminó de concretar el pago inmediato
multa_activa	fallo el pago inmediato y se generó una multa
pagos_extra_pendientes	Pagó el valor pujado (y multa si aplica), pero todavía no las comisiones (y envío si aplica)
pagada	todos los pagos necesarios realizados
entrega_pendiente	Se pago todo y falta que llegue el bien al comprador
retiro_pendiente	Se pago todo y falta que el comprador retire el bien
abandonada_por_incumplimiento_pago	Ocurre cuando la empresa se queda con el bien por falta de pago de comisiones/envío 
abandonada_por_incumplimiento_retiro	Ocurre cuando la empresa se queda con el bien por falta de retiro del mismo
Completada	El comprador ya tiene el bien en su posesión
- Para estos estados de compra, al igual que se puede ver en el proceso general de consignación de debajo, se podría quizás separar en más de 1 enum, para conservar algunos estados en historial y tener una mejor gestión de los mismos.
Recomendación de plazos
•	artículo/multa: pago inmediato o dentro de la excepción simulada
•	comisiones/envío: hasta 72 horas
•	si no paga multa en plazo de 72hs: bloqueo
•	si no paga comisiones/envío en plazo: La empresa se queda con el item (Se considera como abandonado o algo por el estilo, entonces la empresa se queda con eso y listo, sin devolver nada del dinero pagado del valor pujado)
Multa
•	10% del valor ofertado
•	vence en 72 horas
•	si no paga, bloqueo permanente
________________________________________
12) Consignación
Estados recomendados
Estado	Uso
pendiente_revision	submit hecho y la empresa está en proceso de revisarlo para la 1ra aprobación o rechazarlo directamente
rechazo_inicial	La empresa rechazó el artículo en la revisión “digital”
documentacion_adicional	falta respaldo
recepcion_pendiente	Aceptado en fase inicial de validación “digital”, pero pendiente de que el usuario haga llegar el artículo a la empresa para la revisión física.
revision_fisica	bien recibido o en inspección
rechazo_revision_fisica	La empresa rechazó el artículo en la revisión física
acuerdo_pendiente	Revisión física aprobada y empresa propuso condiciones
acuerdo_aceptado	usuario aprobó
acuerdo_rechazado	usuario rechazó
devolucion_pendiente	La empresa tiene posesión del artículo pero no se puso en subasta por alguna razón, sea por parte de la empresa al rechazarlo en revisión física, o sea porque el usuario rechazó el acuerdo
publicada	lista para subasta
en_subasta	ya asignada
vendida	se adjudicó a un cliente normal
comprada_por_empresa	quedó sin pujas, entonces la empresa se hace cargo de adquirirlo al precio base
liquidada	se pagó al dueño
devolucion_incompleta	El usuario no fue a retirar el artículo, ni pagó el envío de devolución en plazo, por lo que la empresa puede hacer lo que vea acorde con el artículo recibido.
(POSIBLEMENTE TAMBIÉN GUARDAR ALGUNOS DE LOS ESTADOS PREVIOS, O SEPARAR ALGUNAS COSAS EN 2 O MÁS ENUMS, COMO PARA QUE DESPUÉS DE QUE QUEDE EN CERRADO, O ALGO POR EL ESTILO, QUE SE PUEDA IGUALMENTE CONSULTAR ESOS PASOS PREVIOS, EN DÓNDE FUE RECHAZADO POR EJEMPLO, ENTRE OTRAS POSIBILIDADES)
Lo que se podría hacer adicionalmente
No usar un único enum gigante si después complica consultas.
Sino quizás mejor dividirlo en varias partes, separando los estados por ejes:
•	estado_general
•	estado_revision
•	motivo_rechazo
•	estado_devolucion
•	estado_subasta
Recomendación
•	una consignación puede tener varios bienes solo si querés hacerlo explícito
•	si no, mejor 1 solicitud por bien
•	“colección” que sea solo un nombre comercial de subasta, no una entidad aparte
Documentación de origen
•	se puede subir al inicio
•	o cuando la empresa la pide
•	si no alcanza, pasa a documentacion_adicional

________________________________________
13) ¿Cuándo crear duenios?
Mi recomendación final es esta:
•	no al subir un artículo
•	no al primer borrador
•	sí cuando la empresa decide aceptar formalmente al usuario como consignador
Eso encaja mejor con verificador NOT NULL y con los campos de evaluación.
- Este proceso general ya fue descrito arriba, en el punto 2, de la descripción general de la tabla duenios
________________________________________
14) Documentos
Documentos principales
•	comprobante de compra
•	recibo de multa
•	acuerdo de consignación
•	liquidación de venta
•	comprobante de pago de envío de devolución
Recomendación técnica
•	generar todo como PDF backend
•	almacenar metadata + archivo
•	no mostrar versiones múltiples al usuario
•	el usuario ve solo la última versión válida
Tamaños recomendados
•	PDF: hasta 10 MB
•	imágenes normales: hasta 8 MB por archivo
•	fotos de consignación: hasta 8 MB por imagen
Retención
•	no borrar documentos automáticamente
•	mínimo recomendado: 5 años
•	ideal: conservarlos mientras el usuario tenga cuenta y además en archivo histórico
________________________________________
15) Notificaciones
Estados
•	no_leida
•	leida
•	archivada
Retención recomendada
•	no leídas: 90 días
•	leídas: 30 días después de leídas
•	documentos: no borrarlos por notificación
Recomendación
•	guardar notificaciones en BD
•	no borrarlas apenas se leen
•	archivar o limpiar en job programado
Eventos que sí notificaría en la app en sí (aparte de los mails enviados)
•	aprobación de usuario
•	aprobación/rechazo de medio de pago
•	inicio de subasta en la que se realizó el proceso de inscripción previamente
•	multa generada
•	consignación aceptada/rechazada/actualización general de estados
________________________________________
16) Auth, tokens y sesiones
Recomendación base (a modo de ejemplo que se pueda alterar si mejoraría el funcionamiento)
•	access token: 15 minutos
•	refresh token: 30 días
•	recovery token: 30 minutos
•	registro final token: 48 horas
•	reenvío de link: invalida el anterior
Login
•	email + contraseña
•	sesión recordada si no hay logout
Contraseña
•	mínimo: 8 caracteres
•	debe tener:
o	1 mayúscula
o	1 número
o	1 símbolo especial
________________________________________
17) Archivos y transporte
Recomendación
•	usar multipart/form-data
•	no usar Base64 salvo caso puntual
•	fotos:
o	JPEG
o	PNG
o	WebP
•	documentos:
o	PDF
Límites
•	DNI frente/dorso: 1 archivo por lado
•	cheque: 2 imágenes
•	consignment: mínimo 6, máximo 15 fotos
________________________________________
18) WebSocket
Yo lo usaría para:
•	pujas
•	cambio de ítem activo
•	cierre de lote
•	notificación de puja superada
•	adjudicación
•	actualización de subasta en vivo
Recomendación (a modo de ejemplo que se pueda alterar si mejoraría el funcionamiento)
•	WebSocket solo para tiempo real crítico
•	HTTP para todo lo demás
•	fallback de polling cada 20–30 segundos solo si el socket falla
________________________________________
19) Admin auxiliar
Sin UI, pero sí con endpoints protegidos.
Yo agregaría endpoints como mínimo para:
•	aprobar/rechazar usuarios
•	aprobar/rechazar medios de pago
•	asignar categoría inicial
•	ajustar puntos
•	crear subastas
•	cargar ítems a subastas
•	aprobar consignaciones
•	pedir documentación adicional
•	simular puja ganadora
•	simular fallo de cobro
•	generar multa manualmente
•	reemitir documentos y mails
•	regenerar documentos
•	Simulación de compra interna de la empresa
(Y POSIBLEMENTE AGREGAR MÁS -  EN GENERAL TODOS LOS QUE SE CONSIDEREN NECESARIOS Y/O ÚTILES)
Protección
•	prefijo /api/admin
•	acceso solo con rol interno o token especial de pruebas

20) Una cosa que seguramente sería necesario agregar: comprador interno de la empresa
Si un ítem queda sin pujas, la empresa lo compra al precio base, pero esa compra necesita un cliente.
Como la empresa no es una persona normal del sistema, yo agregaría una solución de este estilo:
•	un cliente interno/sistema visible solo para backend
•	o una persona técnica especial con su cliente asociado
Eso permitiría registrar:
•	ítems sin pujas
•	adjudicaciones internas
•	cierres correctos de subasta
Posiblemente hacerlo con un cliente interno del sistema o equivalente, por ejemplo:
•	cliente_sistema
•	comprador_empresa
•	cuenta_interna_empresa

21) Algunas consideraciones adicionales sobre estados (Obviamente que se pueden recortar o expandir según se considere necesario, si es que mejoraría el funcionamiento de la app - Igual que como para todo en la app):
________________________________________
21.1) Solicitud de inscripción a subasta
Tabla nueva recomendada: inscripciones_subasta
Estados recomendados:
•	pendiente_validacion_medio
•	aprobada
•	rechazada
Uso:
•	cuando el usuario se inscribe antes de una subasta futura
•	sirve para validar el medio de pago antes de participar
La tabla inscripciones_subasta seguramente debería guardar al menos:
•	usuario/cliente/cuenta
•	subasta
•	medio de pago seleccionado
•	fecha de solicitud
•	estado
•	resultado de validación
•	observación del empleado si fue rechazada
Eso es importante porque la inscripción no es lo mismo que la puja, y una inscripción no determina asistencia a una subasta, sino que unicamente al realizar una puja en una subasta es que se confirma la asistencia a la misma y se registra de manera acorde en la tabla de la base de datos.

________________________________________
21.2) Pujas
Creación de una tabla nueva al no poder modificar ni agregar atributos a la tabla existente (pujos) pero sí necesitar extender su funcionamiento general: 
Estados recomendados:
•	enviada
•	aceptada
•	rechazada
•	superada
•	ganadora
Uso:
•	registrar el resultado real de cada puja
•	útil para websockets, historial y auditoría
________________________________________

21.3) Pago
Tabla nueva recomendada: pagos
Estados recomendados:
•	pendiente
•	procesando
•	aprobado
•	rechazado
Uso:
•	sirve para pagos de artículo, multa, comisiones, envío, devolución, entre otros
________________________________________
21.4) Devolución de consignación
Tabla nueva recomendada: devoluciones_consignacion
Estados recomendados:
•	pendiente_decision_entrega (Si todavia no eligio entre ir a buscarlo o que se haga con envio la devolucion)
•	pendiente_retiro (si eligio retiro pero todavia no lo retiro)
•	pendiente_pago (si eligio envio pero todavia no lo pago)
•	pendiente_entrega (si pago por el envio pero todavia no le llego)
•	completada (ya tiene el articulo de vuelta en su poder el usuario)
•	vencida (la empresa se queda con el articulo y puede hacer lo que vea acorde con el)
Uso:
•	para lo que pasa cuando la empresa tiene el bien en su poder pero no queda en subasta (por rechazar revisión física) o el usuario rechaza condiciones.
22) Consideraciones de negocio generales adicionales
22.1) Cierres e inmutabilidad de valores
Todo lo económico debería guardarse como snapshot al momento del evento y no recalcularse después.
Yo fijaría así:
•	precio base del ítem: se toma el que tenía al momento de publicar la subasta
•	comisión: se congela cuando el ítem entra a subasta
•	moneda: no cambia nunca durante esa subasta
•	datos del vendedor y comprador: se copian al comprobante/factura al momento de emitir el documento
Esto evita que una modificación posterior rompa históricos, facturas o liquidaciones.
22.2) Moneda única por subasta
Lo dejaría totalmente cerrado:
•	una subasta es ARS o USD
•	todo lo que ocurra dentro de esa subasta respeta esa moneda
•	no hay mezcla de monedas dentro de la misma subasta
22.3) Comisión
Yo la modelaría como un porcentaje configurable, con un valor por defecto.
Recomendación:
•	comisión base sugerida (valor que se utiliza si la empresa no ingresa nada): 10%
•	rango editable por empresa: 1% a 99%
•	comisión fijada por ítem
- Adicionalmente, cabe considerar que tenemos 2 comisiones dentro del funcionamiento general, la comision que se cobra al vendedor, y la comision que se cobra al comprador. Entonces, si el empleado de la empresa, al armar el acuerdo de consignacion no introduce nada en dichos atributos, se usará 10% para cada uno por defecto, y si lo desea, puede establecer manualmente las comisiones como vea acorde, pudiendo hacer porcentajes diferentes para cada una de las 2 comisiones. 
comision_vendedor: 1% a 99% (aplica siempre)
comision_comprador: 1% a 99% (no aplica si compra la empresa)
La comision_vendedor, será el porcentaje del valor final ofertado (precio base si nadie oferta) que no se paga al vendedor, sino que se queda la empresa.
La comision_comprador, será el porcentaje del valor final ofertado que debera pagar siempre el comprador luego de aganar una puja, y dentro del plazo predefinido de pago de comisiones (+ envio si aplica). Cabe destacar para esta ultima comision, que en caso de que no se haya pujado por el artículo y el comprador termine siendo la empresa en si, entonces se deberia despreciar dicha comision_comprador y no tenerse en cuenta ni realizar dicho cobro.
CONSIDERACIÓN IMPORTANTE: Obviamente, haria falta una estructura o manejo adicional para estas comisiones, al no tener esta posibilidad de manejo de 2 comisiones diferentes en las tablas ya existentes del sistema actual.
Podría ser algo como:
•	condiciones_economicas_subasta
•	o comisiones_item
•	o acuerdo_consignacion
con snapshots como:
•	comision_vendedor
•	comision_comprador
•	moneda
•	comprador_sistema_exento o similar para el caso sin pujas en que la empresa compra el item.
Esto sería util para poder representar bien:
•	el acuerdo con consignador,
•	el cobro al comprador,
•	y el caso especial en el que compra la empresa.
22.4) Empresa compradora cuando nadie puja
Esto hay que dejarlo explícito en backend.
Cuando un ítem no recibe pujas:
•	la empresa lo compra al precio base
•	eso debe generar un registro contable/comercial real
•	conviene tener un cliente técnico interno o “comprador sistema” para no dejar ese caso sin FK válida
22.5) Trazabilidad manual
Lo recomendable sería que todo lo que revise una persona de la empresa debería guardar:
•	quién lo revisó
•	cuándo
•	qué resolvió
•	qué cambió
Eso aplica a:
•	validación/rechazo de usuario, junto con asignación de categoría
•	validación de nuevo dueño (para la tabla duenios)
•	validación/rechazo de medio de pago
•	revisión de consignación (con todos sus pasos e interacciones posibles por parte de un empleado de la empresa durante el proceso de consignacion)
22.6) Reglas de unicidad
Yo fijaría reglas fuertes desde el inicio:
•	un email no puede tener dos solicitudes de registro activas
•	un documento no puede pertenecer a dos personas activas
•	una cuenta no puede tener dos medios principales de la misma moneda (Únicamente se permite si dichos medios de pago son de monedas diferentes, al nunca poder usarse en una misma subasta por regla de negocio)
•	una subasta no puede tener dos ítems activos simultáneos
•	un usuario no puede tener dos pujas ganadoras activas al mismo tiempo

22.7) Dirección de envío
Yo haría que la dirección de envío se elija antes del pago final de comisiones (+ envío si aplica) y quede bloqueada luego.
Regla recomendada:
•	el usuario puede administrar hasta 5 direcciones
•	solo 1 principal
•	al pagar comisiones + envío, se congela la dirección elegida para esa compra y luego no se podrá cambiar
22.8) Plazos recomendados
Te conviene fijar tiempos concretos para que el backend no quede ambiguo:
•	verificación de medio de pago: 5 días hábiles
•	vigencia de enlace de recuperación de contraseña: 30 minutos
•	aceptación de ítem rechazado y devolución: 72 horas
22.9) Reglas de entrega
Yo separaría:
•	retiro personal
•	envío a domicilio
Y dejaría fijo que:
•	si el usuario elige retiro, la cobertura de seguro termina al entregar el bien
•	si elige envío, el costo se suma a la compra
22.10) Consignación y devolución
Acá conviene dejar claro:
•	si la empresa rechaza un bien, el usuario paga devolución
•	si no paga la devolución, la empresa puede disponer del bien
•	si el usuario rechaza el acuerdo, el bien pasa a flujo de devolución
22.11) Regla para asistentes.numeroPostor
Se manejaría de la siguiente manera:
Un usuario ingresa al apartado de una subasta, y recien cuando realiza una puja es que se confirma su asistencia efectiva; al hacer esto, se crea el registro acorde en la tabla de asistentes (únicamente es una asistencia efectiva cuando se realiza una puja confirmada) y se debera asignar ese numeroPostor. Si todavia no pujo nadie por ningun articulo en esa subasta, entonces el primero que puje por cualquier articulo (gane o no esa puja), tendra numeroPostor = 1. Y a partir de ahi, cada usuario que realice su primera puja en esa subasta (por cualquier articulo), entonces se le asignara el numero siguiente al mayor numeroPostor de esa subasta particular. Es decir, si la primera puja de toda la subasta, se le asigna el numero 1, y si no es el primero en realizar alguna puja, pero si es su primera puja en esa subasta entera, entonces se le asignara: max numeroPostor (de esa subasta particular ) + 1, y a partir de ahi, no se modificara nunca su numeroPostor de esa subasta particular.
Es decir, por cada subasta, se le asignara un numeroPostor (unico dentro de esa subasta particular solamente) a cada usuario que realice al menos 1 puja en esa subasta particular (por cualquier articulo).
Entonces, las reglas generales para asistentes.numeroPostor serían:
•	se asigna en la primera puja aceptada de ese usuario en esa subasta
•	queda fijo para toda esa subasta
•	es único dentro de esa subasta
- También cabe destacar:
•	la inscripción no implica asistencia
•	la asistencia se confirma solo al pujar
•	la inscripción solo sirve para validar medio de pago y dejar constancia operativa
22.12) Sería necesaria una nueva estructura para poder modelar algunos aspectos adicionales de cada subasta, como el título, la moneda y el segmento (diferente de categoría).
Esto es importante porque la tabla subastas existente no tiene esos datos, y la app sí los necesita.
Seguramente con una tabla nueva tipo:
•	subastas_detalle
•	o subastas_configuracion
Ahí guardaría, al menos:
•	titulo
•	moneda (ARS / USD)
•	segmento
•	y otros atributos necesarios
________________________________________
22.13) Sería necesaria una nueva estructura para el orden real de las pujas y el estado vivo de la subasta
La tabla pujos actual no tiene:
•	fecha/hora
•	secuencia
•	estado temporal real de la puja
•	orden de llegada
Y como no se puede tocar esa tabla (ni ninguna de las presentes en el sistema ya existente), yo agregaría una tabla nueva de apoyo, por ejemplo:
•	pujos_eventos
Ahí se podría poner:
•	timestamp
•	secuencia/orden
•	estado de la puja
•	monto
•	subasta
•	ítem
•	postor
•	resultado final
También podría servir para:
•	websockets,
•	historial,
•	auditoría,
•	resolución de conflictos simultáneos.
- Consideración adicional:
Como no se puede tocar/alterar de ninguna manera la tabla pujos, la nueva tabla pujos_eventos seguramente debería guardar algo tipo idempotency_key para evitar dobles envíos por reconexión o reintento. Esto podría ayudar en general para evitar situaciones de duplicados por reconexión, reintentos, y para pujas simultáneas.
________________________________________
22.14) También se podría definir explícitamente el ítem/lote activo actual (PERO ESTO SOLO AGREGARLO SI SE CONSIDERA QUE MEJORARÍA REALMENTE EL FUNCIONAMIENTO DE LA APP)
Se podría agregar una nueva estructura/entidad clara que persista las funciones de “puja actual” y “lote en vivo”
Posiblemente agregar una estructura auxiliar, por ejemplo:
•	subasta_estado_actual
Ahí se podría guardar:
•	subasta actual
•	item/lote activo
•	mejor puja actual
•	hora de inicio del lote
•	hora de fin estimada
•	estado del lote

— CONSIDERACIÓN IMPORTANTE SOBRE las nuevas posibles tablas de pujos_eventos y subasta_estado_actual: 
- pujos sigue siendo el registro y resultado económico/operativo principal
- pujos_eventos es el log de eventos y orden
- subasta_estado_actual es una tabla auxiliar tipo snapshot, solo para estado vivo. 
Cabe destacar esto y aplicarlo de manera correcta y acorde en el funcionamiento principal para luego no quedar con dos fuentes de verdad contradictorias.

22.15) Sería necesario también agregar algunas estructura para fotos, adjuntos y documentos generales del flujo de registro, consignación u otros
En el sistema actual, ya se encuentra la estructura general para almacenar fotos de productos. A pesar de esto, seguramente sería necesaria alguna nueva estructura auxiliar para almacenar las fotos de solicitudes_consignacion antes de que se pueda crear su registro acorde en la tabla de productos. Esto se debe a que antes de que se llegue al acuerdo de consignacion y el usuario lo acepte, tampoco se puede crear el registro acorde en la tabla de productos, y por ende tampoco se puede crear ningún registro acorde en la tabla de fotos ya existente en la estructura actual SQL. Y además de ese proceso particular, seguramente harían falta otras estructuras para almacenar fotos y documentos del resto de aspectos de la app.
Esto se podria adaptar para cosas como fotos DNI, documentos de origen, fotos de consignación y documentos generados:
Seguramente agregaría al menos tablas nuevas para:
•	adjuntos de solicitudes_registro
•	adjuntos de solicitudes_consignacion
•	documentos generados
•	(posiblemente también algo por el estilo para las fotos de los cheques)
Es decir, se crearán una serie de tablas hijas para las fotos, archivos y docs de varios apartados; Todo lo necesario como guardar las fotos de dni del registro, fotos de cheques, fotos/documentacion adicional a modo de documentacion de origen de un articulo a consignar, algo para guardar, al menos temporalmente, las fotos subidas de un artículo para consignar (antes de la creación del registro acorde en la tabla productos). 
Y adicionalmente, si se considera acorde y necesario, posiblemente también crear alguna o varias estructuras auxiliares para toda la documentación generada por la app, es decir, para cubrir al menos los siguientes documentos:
- Comprobante de compra (Comprador)
- Recibo de multa (Comprador)
- Acuerdo de consignación (Vendedor)
- Liquidación de venta (Vendedor)
- Comprobante de pago de envío de devolución

22.16) Recomendación general mínima de auditoría (Se puede recortar o extender según se vea acorde si mejoraría el funcionamiento general)
Yo guardaría auditoría de:
•	login/logout
•	alta y aprobación de usuario
•	alta, aprobación y vencimiento de medios de pago
•	pujas
•	adjudicaciones
•	multas
•	pagos
•	cambios de estado
•	revisión de consignaciones
•	generación de documentos

