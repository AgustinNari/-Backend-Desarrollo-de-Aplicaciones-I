# Material fuente: consigna_y_sql_legacy_original.md

> Este archivo conserva el texto original recibido para que Codex tenga acceso a todos los detalles, incluso los que no estén resumidos en la documentación central.

---

Consignas formales en si de la aplicacion:

 Sistema de Subastas

	El equipo de analistas ha finalizado el relevamiento para una app que gestione la inclusión de artículos en subastas y la participación como oferentes en estas. 
	
La empresa solicito un desarrollo para dispositivos móviles que les permita a los usuarios participar de forma on-line en las subastas que realiza la empresa en forma presencial, como así también indicar que posee algún artículo que deseen incluir en futuras subastas. 
	
La empresa posee actualmente un sistema local que contiene toda la información de las subastas realizadas, los dueños de los ítems en subasta de cada subasta, los postores, las ofertas realizadas por cada uno de los postores (concretadas o no), los rematadores o martilleros, etc. Nuestra app deberá consumir y actualizar esa información a sind e integrarse con el sistema existente.
	
	Una subasta (o remate) es una competencia de ofertas donde gana el que más paga. Se parte de un valor inicial (conocido como precio base) y el mejor postor se queda con el artículo.

	El postor es una persona que participa en una subasta ofreciendo dinero para intentar comprar el objeto que se está subastando.

	La puja es hacer una oferta de dinero para tratar de comprar el objeto que se está rematando (cada vez que un postor hace una oferta es una puja).
 
La empresa en cuestión realiza las subastas en la modalidad conocida como subasta dinámica ascendente donde los postores conocen las ofertas de su competencia y pueden modificar la suya mientras la subasta está abierta. 

Esta modalidad parte de un precio de reserva o base y los postores van presentando ofertas con precios ascendentes, ganando quien ofrezca el precio mayor. 

La aplicación móvil requiere que los postores se encuentren registrados para poder participar y se identifiquen antes de su participación. 

El mecanismo de registración de los postores se realiza en dos etapas, la primera donde el postor ingresa sus datos como nombre, apellido, foto del documento (frente y dorso), domicilio legal y país de origen. 

Estos datos son verificados por la empresa de subastas mediante una investigación externa y si se lo acepta se le asigna una categoría de acuerdo con la investigación realizada para autorizarlo. 

Las categorías son común, especial, plata, oro y platino, esta categoría determina en que subastas puede participar. 

Una vez finalizada la primera parte de registración, se le envía un mail informándole que debe ingresar a la app y completar el registro y generar su clave personal.  

A continuación, el usuario debe registrar al menos un medio de pago, pero puede registrar todos los que desee y gestionarlos a través de la app. 

Estos pueden ser cuentas bancarias (pueden ser bancos extranjeros) con fondos reservados para la subasta, tarjetas de crédito (nacionales o extranjeras) o cheques certificados por un monto determinado entregado y verificado ANTES del inicio de la subasta.

La diversidad de los medios de pago del usuario y su actividad en las subastas permiten mejorar su categoría.

Cada subasta tiene asignado un día y un horario, una categoría, un rematador, y una lista de objetos a subastar denominada catálogo. Los catálogos son públicos, pero solo los usuarios registrados (de cualquier categoría) pueden ver su precio base de venta.

De los objetos que conforman el catálogo conocemos su número de pieza (o ítem), una descripción de este (un pequeño texto que lo describe), el precio base, el dueño actual y una serie de imágenes de este (aproximadamente 6). Hay que tener en cuenta que una pieza o ítem puede estar formado por varios elementos (Juego de Te de 18 piezas)

En el caso de que sean obras de arte u objetos de diseñador se conoce el nombre del artista o diseñador, la fecha y la historia del objeto (contexto, dueños anterior, curiosidades, etc.).
Para que un postor pueda acceder a una subasta determinada debe encontrarse registrado y la categoría de la subasta debe ser menor o igual que la propia. 

Solo podrá pujar en la misma si tiene al menos un medio de pago verificado por la empresa. Caso contrario solo podrá ver la subasta.

Para los usuarios que utilicen la aplicación, la empresa brindará un servicio de streaming para poder seguir el desarrollo de esta, este servicio no forma parte del desarrollo de la app. Cualquier usuario registrado y aprobado puede acceder al servicio.

El usuario seleccionara a cuál de las subastas abiertas existentes se desea conectar. El acceso a las subastas esta dado al poseer un medio de pago registrado y por la categoría de la subasta y la del usuario.

Al ingresar a una subasta el usuario podrá ver que artículo se subasta y cuál es la mayor oferta hasta el momento. 

Si cumple con las condiciones, el usuario puede pujar en la subasta determinando la cantidad que desea ofertar (debe ser mayor a la mejor oferta hasta el momento) y pasando está a ser la mayor hasta que aparezca una mejor. 

El monto de la puja debe ser al menos el mejor valor hasta el momento más el 1% del valor del valor base del bien. Por ejemplo, un bien que tiene un valor base de 10.000 y la ultima oferta fue de 15.000, la puja debería ser al menos 15.100.

Por otro lado, el monto de la puja no puede ser mayor al valor de la ultima oferta mas el 20% del valor base del bien. Por ejemplo, un bien que tiene un valor base de 10.000 y la última oferta fue de 15.000, la puja máxima sería 17.000.

Estos límites no aplican a las subastas de categorías oro y platino

Los usuarios conectados deben recibir en tiempo real las modificaciones de las ofertas para poder hacer ellos sus propias ofertas y que la app las valide antes de ser enviadas.

Cuando ya nadie puja con un valor más alto, el usuario de la última puja pasa a ser el nuevo dueño de la pieza. 
Se registra la venta del objeto con el medio de pago seleccionado y los datos del usuario. La pieza se marca como vendida y se actualizan todos los datos (registración del nuevo dueño, importes, comisiones, etc.).

Se le informa por medio de un mensaje privado el importe que debe pagar indicando lo pujado, las comisiones y el costo de enviarlo a la dirección declarada. 

El usuario puede retirar personalmente el bien adquirido, pero en ese caso una vez retirado pierde la cobertura del seguro.
 
Hay que tener en cuenta que si el usuario dejo como garantía de pago un monto de dinero (por ejemplo, un cheque certificado) sus compras no pueden superar dicho monto, pero mientras le alcance puede participar en tantas subastas como quiera.

Si al momento de pagar el usuario no posee el dinero para cumplir con el pago, el usuario recibirá una multa equivalente al 10% del valor ofertado que deberá abonar antes de poder participar en otra subasta, además deberá presentar antes de las 72hs los fondos necesarios para pagar la oferta realizada. 

Si el usuario no cumple con su obligación de pago, el caso se deriva a la justicia  quedando fuera del alcance de esta aplicación, aunque el usuario podrá acceder a ninguno de los servicios de la aplicación.
 
	La empresa puede hacer varias subastas al mismo tiempo, pero los usuarios no pueden estar conectados en más de una a la vez. 

Las subastas pueden ser en pesos o en dólares. Esto está determinado para cada subasta en particular al momento de crear la misma, no es posible hacer una subasta bimonetaria (pagar con dos especies distintas de monedas). 

En el caso de las subastas en dólares las mismas debe ser canceladas en dicha moneda (ya se por transferencia o por una tarjeta internacional)
	
	De cada subasta se conocen todos sus datos, desde la ubicación, la fecha y hora de inicio, el subastador, etc. y se deben guardar todos los pujes realizados por cada usuario, respetando el orden de estos. 

Se debe garantizar que los pujes de los usuarios están registrados correctamente, por lo que cuando un usuario hace un puje la aplicación no debe permitir otro hasta haber recibido la confirmación por parte del sistema que la transacción fue realizada con éxito e informado al resto de los usuarios.

	Cada usuario puede ver su participación en las subastas, la cantidad a la que asistió, las veces que gano, el historial de pujos de una subasta, etc. La aplicación debería dar una serie de métricas sobre categorías de subastas, participaciones, importes pagados y ofertados, métricas sobre veces que gano una subasta, etc. 

	Por otra parte, los usuarios pueden solicitar a la empresa que coloque algún artículo de su propiedad en subasta. 

Para ellos deberá ingresar los datos del bien a subastar, fotos de este (al menos 6), y cualquier otro dato de interés o histórico que pueda ser relevante. Los usuarios deben declarar que el bien a subastar le pertenece y no posee ningún impedimento para hacerlo (en el formulario de carga deberá tener un casillero que cumpla con este requisito)

Además, debe poder acreditar el origen licito de los bienes a subastar (en el caso de que fueran requeridos). La empresa en caso de duda deberá avisará a las autoridades sobre dudas en el origen.

Si la empresa está interesada en los mismos, el usuario deberá enviarlos a la dirección que le indiquen para proceder a la inspección de estos. Se debe aclarar que el usuario esta de acuerdo que en caso de no aceptar el bien enviado la empresa lo devolverá con cargo al usuario. 

Si la cantidad de artículos a vender es muy numerosa, la empresa puede determinar que es conveniente hacer una sola subasta con esos objetos, en ese caso se suele denominar la subasta como colección con el nombre del usuario. 

	Una vez que la empresa inspecciona el bien puede aceptarlo o no informándolo a través de la app. 

Si no lo acepta el bien es devuelto a su dueño con cargo pudiendo visualizar a través de la app ver las causas del rechazo.

Si lo acepta, el bien se incluye en una futura subasta informándole al usuario la fecha, hora, lugar, el valor base de cada objeto aceptado y las comisiones. 

El usuario puede no aceptar el valor base o las comisiones a cobrar por el bien, en ese caso se procederá a la devolución y se le informará de los gastos. 

	Cuando un usuario participa de una subasta y adquiere un bien, el envío de este está a cargo del comprador y se incluye en el detalle de la factura de compra.

	Si nadie puja por un artículo, la empresa compra el mismo por el valor base al finalizar la subasta.

	El dinero resultado de los artículos vendidos de un cliente, se envía a una cuenta a la vista de que puede ser del exterior. Las cuantes deben ser declaradas antes del inicio de la subasta.

De cada bien recibido para la venta se le contrata un seguro en función del valor base del bien. El seguro puede ser realizado sobre varias piezas, pero siempre que estas sean de un mismo dueño, ya que este será el beneficiario de la póliza.

La aplicación deberá permitir al dueño de una pieza entregada para la subasta ver la ubicación de la pieza (en que depósito se encuentra) y la póliza de seguro que contrato la empresa de subastas. 

El cliente si lo desea puede ponerse en contacto con la compañía de seguros que hizo la póliza y aumentar el valor de la póliza pagando la diferencia del premio.



Estructura Actual SQL de los otros sistemas, de cierta forma ajenos a mi app en si, pero que de cierta manera, deberia incluir y considerar en mi app. PERO IMPORTANTE, NO SE PUEDE MODIFICAR NI AGREGAR NADA SOBRE ESAS TABLAS YA CREADAS, es decir, puedo agregar nuevas tablas unicamente, que pueden tener relaciones con las tablas ya creadas, pero no puedo agregar ni modificar cosas en las tablas ya existentes, osea puedo crear nuevas tablas y estructuras alrededor de esto ya existente, pero no puedo tocar de ninguna manera esto que ya esta actualmente:

create table paises(
	numero int not null,
	nombre varchar(250) not null,
	nombreCorto varchar(250) null,
	capital varchar(250) not null,
	nacionalidad varchar(250) not null,
	idiomas varchar(150) not null,
	constraint pk_paises primary key (numero)
)




go
create table personas(
	identificador int not null identity,
	documento varchar(20) not null,
	nombre varchar(150) not null,
	direccion varchar(250),
	estado varchar(15) constraint chkEstado check (estado in ('activo', 'inactivo')),
	foto varbinary(max)
	constraint pk_personas primary key (identificador)
)



go
create table empleados(
	identificador int not null,
	cargo varchar(100),
	sector int null,
	constraint pk_empleados primary key (identificador)
)
go




create table sectores(
	identificador int not null identity,
	nombreSector varchar(150) not null,
	codigoSector varchar(10) null,
	responsableSector int null,
	constraint pk_sectores primary key (identificador),
	constraint fk_sectores_empleados foreign key (responsableSector) references empleados
)
go



create table seguros(
	nroPoliza varchar(30) not null.
	compania varchar(150) not null,
	polizaCombinada varchar(2) constraint chkpolizaCombinada check(polizaCombinada in ('si','no')),
	importe decimal(18,2) not null constraint chkImporte check (importe > 0),
	constraint pk_seguro primary key (nroPoliza)
)
go


	
create table clientes(
	identificador int not null,
	numeroPais int,
	admitido varchar(2) constraint chkAdmitido check(admitido in ('si','no')),
	categoria varchar(10) constraint chkCategoria check (categoria in ('comun', 'especial', 'plata', 'oro', 'platino')),
	verificador int not null,
	constraint pk_clientes primary key (identificador),
	constraint fk_clientes_personas foreign key (identificador) references personas,
	constraint fk_clientes_empleados foreign key (verificador) references empleados (identificador),
	constraint fk_clientes_paises foreign key (numeroPais) references paises (numero)
)
go


create table duenios(
	identificador int not null,
	numeroPais int,
	verificaciónFinanciera varchar(2) constraint chkVF check(verificaciónFinanciera in ('si','no')),
	verificaciónJudicial varchar(2) constraint chkVJ check(verificaciónJudicial in ('si','no')),
	calificacionRiesgo int constraint chkCR check(calificacionRiesgo in (1,2,3,4,5,6)),
	verificador int not null
	constraint pk_duenios primary key (identificador),
	constraint fk_duenios_personas foreign key (identificador) references personas,
	constraint fk_duenios_empleados foreign key (verificador) references empleados (identificador)
)
go



create table subastadores(
	identificador int not null,
	matricula varchar(15),
	region varchar(50),
	constraint pk_subastadores primary key (identificador),
	constraint fk_subastadores_personas foreign key (identificador) references personas
)
go



create table subastas(
	identificador int not null identity,
	--las subastas tiene al menos 10 dias de anticipación al momento de crearlas.
	fecha date constraint chkFecha check (fecha > dateAdd(dd, 10, getdate())),
	hora time not null,
	estado varchar(10) constraint chkES check (estado in ('abierta','cerrada')),
	subastador int null,
	--direccion de don de se desarrolla el evento.
	ubicacion varchar(350) null,
	capacidadAsistentes int null,
	--caracteristica del lugar donde se hacen las subastas
	tieneDeposito varchar(2) constraint chkTD check(tieneDeposito in ('si','no')),
	--caracteristica del lugar donde se hacen las subastas
	seguridadPropia varchar(2) constraint chkSP check(seguridadPropia in ('si','no')),
	categoria varchar(10) constraint chkCS check (categoria in ('comun', 'especial', 'plata', 'oro', 'platino')),
	constraint pk_subastas primary key (identificador),
	constraint fk_subastas_subastadores foreign key (subastador) references subastadores(identificador)
)
go

create table productos(
	identificador int not null identity,
	fecha date,
	disponible varchar(2) constraint chkD check (disponible in ('si','no')),
	--se obtiene despues que un empleado realiza la revision.
	descripcionCatalogo varchar(500) null default 'No Posee',
	--url que apunta a un documento PDF firmado que contiene la descripción del producto.
	descripcionCompleta varchar(300) not null,
	revisor int not null,
	duenio int not null,
	seguro varchar(30) null,  
	constraint pk_productos primary key (identificador),
	constraint fk_productos_empleados foreign key (revisor) references empleados(identificador),
	constraint fk_productos_duenios foreign key (duenio) references duenios(identificador)
)


go
create table fotos(
	identificador int not null identity,
	producto int not null,
	foto varbinary (max) not null,
	constraint pk_fotos primary key (identificador),
	constraint fk_fotos_productos foreign key (producto) references productos(identificador)
)
go


create table catalogos(
	identificador int not null identity,
	descripcion varchar(250) not null,
	subasta int null,
	responsable int not null,
	constraint pk_catalogos primary key (identificador),
	constraint fk_catalogos_empleados foreign key (responsable) references empleados(identificador),
	constraint fk_catalogos_subastas foreign key (subasta) references subastas(identificador),
)
go

create table itemsCatalogo(
	identificador int not null identity,
	catalogo int not null,
	producto int not null,
	precioBase decimal(18,2) not null constraint chkPB check (precioBase > 0.01),
	comision decimal(18,2) not null constraint chkC check (comision > 0.01),
	subastado varchar(2) constraint chkS check (subastado in ('si','no')),
	constraint pk_itemsCatalogo primary key (identificador),
	constraint fk_itemsCatalogo_catalogos foreign key (catalogo) references catalogos,
	constraint fk_itemsCatalogo_productos foreign key (producto) references productos
)
go


create table asistentes(
	identificador int not null identity,
	numeroPostor int not null,
	cliente int not null,
	subasta int not null
	constraint pk_asistentes primary key (identificador),
	constraint fk_asistentes_clientes foreign key (cliente) references clientes,
	constraint fk_asistentes_subasta foreign key (subasta) references subastas
)
go

create table pujos(
	identificador int not null identity,
	asistente int not null,
	item int not null,
	importe decimal(18,2) not null constraint chkI check (importe > 0.01),
	ganador varchar(2) constraint chkG check (ganador in ('si','no')) default 'no',
	constraint pk_pujos primary key (identificador),
	constraint fk_pujos_asistentes foreign key (asistente) references asistentes,
	constraint fk_pujos_itemsCatalogo foreign key (item) references itemsCatalogo
)
go

create table registroDeSubasta(
	identificador int not null identity,
	subasta int not null,
	duenio int not null,
	producto int not null,
	cliente int not null,
	importe decimal(18,2) not null constraint chkImportePagado check (importe > 0.01),
	comision decimal(18,2) not null constraint chkComisionPagada check (comision > 0.01),
	constraint pk_registroDeSubasta primary key (identificador),
	constraint fk_registroDeSubasta_subastas foreign key (subasta) references subastas,
	constraint fk_registroDeSubasta_duenios foreign key (duenio) references duenios,
	constraint fk_registroDeSubasta_producto foreign key (producto) references productos,
	constraint fk_registroDeSubasta_cliente foreign key (cliente) references clientes
)
go




Esto seria una definicion general de los flujos principales de la app, para invitado y para usuario registrado normal, con varias posibilidades y demas, pensa que seguramente no sea algo perfecto ni tampoco debe ser necesariamente exactamente asi, pero es en general para que tengas al menos una idea general de como esta pensado el flujo, algunas funciones y apartados de la app, etc:

— Flujo de Invitado:

1.Splash

2.Pantalla de selección de login/registro/invitado

3. Elige invitado. Subastas (Ve primero las activas, y despues las futuras) Posiblemente agregar 1/2 botones en cada tarjeta de cada subasta particular, 1 podria ser para entrar directo al catalogo de la subasta, y otro para la puja actual, es decir, la vista general, o quizas que se acceda a esto al presionar sobre cualquier otra ubicacion de la tarjeta

3.a.1.Menú lateral

3.b.1.Entrar a una subasta en particular. Subasta centrada en puja actual PERO LOS ARTÍCULOS MOSTRADOS SIN SU PRECIO, TAMPOCO PUDIENDO VER LAS PUJAS EN VIVO, NADA DEL PROCESO EN SI

3.b.a.1.Quiere pujar o ver algo más.Popup explicando que no puede al no estar logueado

3.b.b.1.Detalles de subasta si quiere ver el resto del catalogo de esa subasta, pudiendo observar pujas ya pasadas y las futuras, con el detalle de cada producto

3.b.b.2.¿Obra de arte u objetos de dieñador?

3.b.b.2.a.1.Si, Mostrar datos adicionales acordes

3.b.b.2.b.1.No, Con los datos comunes alcanza

3.a.1.a.1.Apartado de Ayuda/Soporte/FAQ

3.a.1.b.1.Apartado de Notificaciones y Comprobantes

3.a.1.c.1.Metodos de Pago

3.a.1.d.1.Historial - Estadísticas/Métricas

3.a.1.e.1.Consignar Bien

3.a.1.f.1.Perfil

3.a.1.g.1.Sector de Pagos - Sector de Compras

3.a.1.b,c,d,e,f,g.2.Popup explicando que no puede acceder a esto al no estar logueado










— Flujo de Usuario Normal:



1.Splash
2.Login 
3.¿Registrado?

3.a.1.No.¿Registro 1 realizado?

3.a.1.a.1.No.Pantalla de registro 1

3.a.1.a.2.Completa 1er registro.msj todo ok vueva despues de verificacion (Se le envia el codigo al mail). Si presiona cancelar o algo similar, vuelve a ingresar sus datos y el prceso de verificacion y demas ya esta completo, es decir, que ya se le realizó la verificación, asignaciones y demás, con mail enviado y demas, entonces puede seguir

3.a.1.b.1.Si, ¿ingresa algunos datos?.¿Usuario validado y con datos asignados por empresa? (Si es rapido o instantaneo el proceso de verificaciond de cuenta, asignacion de categoria y demas, entonces podria llegar aca desde haber recien hecho el registro 1, pero tampoco seria algo realista en el uso normal y real de la app, porque seria seguramente un proceso manual que toma cierto tiempo, al menos horas o dias seguramente, para que la empresa de subastas revise y verifique todo del usuario bien)

3.a.1.b.1.a.1.No.Todavia no puede completar el registro

3.a.1.b.1.b.1.Si.Pantalla para registrar código enviado por mail. Posiblemente agregar algun enlace en el mail enviado de confirmacion de registro en si, como para que cuando la empresa verifica y asigna todo, entonces le manda el mail, y dentro de este, tiene una redirección para completar el registro total

3.a.1.b.1.b.2.Pantalla de creación de nueva clave/contraseña y terminar la 2da etapa de registro

3.a.1.b.1.b.3.Pantalla para ver si le interesa pujar y/o vender, con posibilidad de skipear y advertencias acordes

3.a.1.b.1.b.3.a.1.Skip.Subastas (Ve primero las activas, y despues las futuras)

3.a.1.b.1.b.3.b.1.Interesado en pujar y/o vender.Metodos de Pago (Aca se lo redirige directo a los metodos de pago, para registrar al menos 1, ya que tanto para poder pujar y/o poder vender, es necesario tener al menos 1 metodo de pago registrado y verificado, y esa verificacion tambien seria seguramente manual por parte de la empresa de subastas, por lo que tomaria cierto tiempo, y lo mejor seria hacerlo apenas se registra, para no demorar despues)

3.b.1.Si.Login

3.b.2.Inicia sesion.¿Cuenta bloqueada?

3.b.2.a.1.Si.Ventana acorde, sin poder hacer nada hasta arreglar el bloqueo

3.b.2.b.1.No.Subastas (Ve primero las activas, y despues las futuras)


Bien ahora, cabe considerar que todo forma parte del mismo flujo, es decir, se arranca desde splash screen, se inicia sesion o completa el registro, y se termina en algun lado acorde, por lo general, salvo los casos ya descritos, se termina, es decir, lo principal de la app donde se inicia su uso de verdad, seria la vista/ventana de subastas, entonces, ahora, para que sea mas simple la nomenclatura, vamos a agregar nuevos numeros para cada parte/ventana mas separada desde este punto una vez ya en la ventana de subastas, pudiendo ver listas de estas y demas.

4. Menu desplegable (Para la navegacion, seria con un menu desplegable, que permitira desplazarnos a cada vista mas grande directamente, y ademas de esto, seguramente se agregaria un nav en la parte de debajo con aprox 5 de los apartados mas importantes/principales de la app, quizas incluir 4 apartados grandes y que el 5to boton sea para desplegar el menu en si para el acceso al resto de apartados de la app)

5.Apartado de Ayuda/Soporte/FAQ

6.Apartado de Notificaciones y Comprobantes - Aca, seguramente seria sin notificaciones push, pero si se deberia tener algun lugar donde se puedan ver notificaciones, algunos eventos que envia la app y demas posiblemente, aunque esto igual no seria tan obligatorio, E IMPORTANTE REVISAR SI ESTO DE VERDAD SE HARIA, PORQUE HAY QUE ESTRUCTURAR SEGURAMENTE ESTO, GUARDARLO EN TABLAS Y DEMAS PARA PODER HACERLO BIEN, POR LO QUE REVISAR DESPUES SI ESTO SE HARIA DE VERDAD O NO

7.Metodos de Pago - Esto seria seguramente con una lista con tarjetas, 1 para cada metodo de pago registrado, con opciones de poder eliminar o editar sobre cada uno de esos, etc. y ademas, poder agregar nuevos metodos de pago. ADEMAS, CONSIDERAR QUE SE DEBE PODER ELEGIR SEGUN EL TIPO DE MONEDA DEL METODO DE PAGO, Y ADEMAS DE ESO, ESPECIFICAMENTE PARA LOS CHEQUES, ADEMAS DE INGRESAR LOS DATOS EN UN FORMULARIO NORMAL, ESCRITOS Y DEMAS, SE DEBE AGREGAR 1 O 2 IMAGENES DE ESE CHEQUE, COMO PARA TENER UNA VERIFICACION DEL MISMO, ESTO AL AGREGARLO COMO METODO DE PAGO (QUIZAS FRENTE Y DORSO, POR ESO 2 IMAGENES QUIZAS)

7.a.1.¿Eliminar metodo de pago?

7.b.1.Modificar

7.c.1.Agregar

Considerar esto tanto para el momento de agregar como al modificar un metodo de pago(Una vez agregado el metodo de pago nuevo, se debe mostrar un mensaje de que se debe verificar antes de poder usarlo, y posiblemente mandar un mail cuando la empresa lo haga y esté todo bien con ese método de pago)

8.Historial - Estadísticas/Métricas (Quizas podria hacerse algo integrado, con 2 subapartados, 1 para ver historial general de las subastas en las que se participo y demas, pujas ganadas, perdidas o cosas por ese estilo, y otro apartado mas dedicado a metricas/estadisticas mas centrada en numeros y demas)

9.Perfil - Posiblemente asociar, dentro del perfil, también lo de historial y métricas, o quizás dejarlo separado. Además de gestiones normales de perfil, datos y demas, aca tambien podria llegar a ir el contador de puntos para subir de categoria como usuario, donde se indique que hace falta y demas como para poder subir de categoria, la idea seria que ciertas acciones, como agregar nuevos metodos de pago, con variedad de tipos y monedas por ej., ganar pujas, participar en subastas y demas, que esas cosas puedan sumar puntos, y el hecho de no hacer pagos a tiempo, registrar multas y demas, eso probablemente que reste puntos, todo esto para tener un acumulado de puntos, que funcionarian de manera que si se tiene X puntos, uno pertenece a X categoria y listo, para que el sistema sea siempre objetivo y listo, bueno exceptuando cuando la empresa de subastas realiza la 1ra asignacion de categoria, donde uno puede empezar mas arriba o debajo en categoria segun lo defina la empresa al analizar el perfil, pero sacando eso, seria todo objetivo, con ese sistema de puntos que se van sumando y restando segun las acciones en la app

10.Sector de Compras - Sector de Pagos (Aca el sector de compras, seria seguramente del estilo del sector de metodos de pago, con tarjetas para cada cosa, pero en este caso seria 1 tarjeta de esas para cada compra/puja ganada realizada, donde se pueda hacer gestion de estos, poder elegir si retirar o no, poder pagar por el comisiones (+ envio si aplica), pagar por una multa si no se efectuo bien el pago al ganar la puja, y en ese caso, pagar tambien por el articulo del que se gano la puja en si. Establecer la direccion y detalles de entrega para envio y demas si aplica, poder ver el estado del mismo, y demas detalles y posibilidades de gestion como para compras en general). Adicionalmente, en este sector, cuando ya se hizo pago de articulo + comisiones (y envio si aplica), entonces se generara una factura, y ademas de que esto se manda al mail,  esta factura debe poder verse en el apartado de cada compra especifica que cumpla esa condicion. Adicionalmente, para los articulos que se haya pagado alguna multa, ademas de enviar el recibo de pago de esa multa al mail acorde, tambien poder ver el recibo dentro de ese apartado de compras de ese articulo por el que se pago una multa en si, como para las posibles facturas. Es decir, cuando es acorde, ademas del envio de mail, deben poder verse esos 2 documentos en el apartado de cada puja ganada, segun aplique cada caso.

11.Subastas (Ve primero las activas, y despues las futuras) - En esto, se veria una especie de lista, o quizas mejor un carrusel horizontal, con 1 tarjeta para cada subasta, quizas poder buscar, o incluso poder filtrar por algunas caracteristicas sobre estas subastas. En estas tarjetas seria donde se vean los detalles acordes, y quizas 1 boton para ver el catalogo directo, y que al presionar en el resto de la tarjeta, se ingrese de manera acorde a la subasta en si, viendo el articulo en si que se esta subastando en ese momento, como para poder subastar ahi y demas si todas las condiciones van bien, y esto tambien en caso de ser una subasta que esta activa en ese momento.. 

11.1.Entrar a una subasta en particular.Subasta centrada en puja actual
(IMPORTANTE: Cabe considerar que al momento de entrar a una subasta, si es una subasta todavia no empezada, es decir, que arrancara en cierto plazo, al entrar a la misma, si ingresa a su apartado al menos 30 min antes de que comience, entonces podra hacer un registro/inscripcion para dicha subasta, donde, ademas de posiblemente recibir notificacion cuando arranque y demas posibilidades, lo importante seria que en esa inscripcion, el usuario puede elegir un metodo de pago con el que realizar ese registro, de moneda acorde, y luego, se supone que en el caso real, manualmnete, algun empleado de la empresa de subastas debera verificar/comprobar ese metodo de pago, para que despues ese usuario pueda usar ese metodo de pago ya verificado al momento de que arranca la subasta, para poder pujar, y bueno, quizas no necesariamente en esa subasta, sino que lo podria usar en todas las subastas de esa moneda, mientras que el usuario cumpla con el resto de requisitos obviamente, y que sean de esa misma moneda, ya que una vez que se tiene el metodo de pago validado/verificado, entonces, estableceremos un plazo de 3 dias habiles (o un numero aprox a esto) durante los cuales sera valida esa verificacion, es decir, ese metodo estara como "verificado" durante esos 3 dias habiles, y despues, tendra que repetir el proceso para que se valide y poder usarlo en subastas acordes, es decir, volver a realizar ese pre registro/inscripcion a otra subasta, con ese metodo de pago, que se valide y demas detalles acordes. 

En general parece que sería así, tenes x métodos de pago registrados, al inscribirse a una subasta todavía no empezada, se indica con qué método de pago estaría interesado en participar, y alguien de la empresa de subastas deberá después validar eso, una vez hecho eso, se puede establecer internamente un tiempo que dure esa validación (3 dias habiles), pero una vez validado este método de pago y dentro del plazo que se establezca, podrá hacer lo que quiera con ese método de pago, pujar en otras subastas que no se haya inscrito directamente, hacer pagos de comisiones + envíos, etc. Todo verificando los límites y que se use la moneda acorde para cada cosa obvio. Y bueno, al pujar, debe elegir entre sus métodos de pago validados y acordes, seguramnete un dropdown y que se use el seleccionado en las pujas que realice, es decir, se elige eso del dropdown, se queda elegido, y mientras no lo cambie, todas las pujas que realice en esa subasta, seran con ese metodo de pago. Es decir, una vez que tenes el método de pago validado y esa validación vigente, entonces tenes bastante vía libre con eso. Lo único es que necesitarías inscribirte a diferentes subastas para que se validen todos los métodos de pago posibles y poder usar todos los que se tengan registrados. Por último, no hace falta que el método de pago de la puja sea el mismo que de una posible multa ni de las comisiones + envío, pero si validado obviamente.)

11.1.a.1.Detalles de subasta si quiere ver el resto del catalogo de esa subasta, pudiendo observar pujas ya pasadas y las futuras, con el detalle de cada producto

11.1.a.2.¿Obra de arte u objetos de dieñador?

11.1.a.2.a.1.Si.Mostrar datos adicionales acordes

11.1.a.2.a.2.No.Con los datos comunes alcanza

11.1.b.1.Quiere pujar.¿Multa o artículo pendiente sin pagar? (DENTRO DE TODO ESTO, INTEGRAR LAS NOTIFICACIONES Y COMPROBACIONES EN TIEMPO REAL, PARA QUE NO SE SOBREPASEN LAS PUJAS ENTRE SÍ, QUE SE NOTFIQUE DE CADA PUJA EN TIEMPO REAL Y QUE NO HAYA CONFLICTOS ENTRE PUJAS EN MOMENTOS CERCANOS EN EL TIEMPO, ETC.)

11.1.b.1.a.1.Si.Popup indicando que no puede pujar hasta pagar todas sus multas pendientes

11.1.b.1.b.1.No.¿Ya está con una puja activa ganadora en otra subasta?

11.1.b.1.b.1.a.1.Si.Popup indicando que no se puede estar pujando en 2 subastas al mismo tiempo

11.1.b.1.b.1.b.1.No.¿Categoría propia mayor o igual a la de la subasta?

11.1.b.1.b.1.b.1.a.1.No.Pop up de puja en subasta restringido por categoría

11.1.b.1.b.1.b.1.b.1.Si.¿Método de pago registrado, verificado y además con moneda acorde? (Necesariamente metodo de pago en pesos para subastas en pesos, y en dolares para subastas en dolares. ADEMÁS, IMPORTANTE aplicar límites según el máximo del método de pago aplicable que haya seleccionado, si lo intenta superar, no se le permite, y se muestra un pop up acorde)

11.1.b.1.b.1.b.1.b.1.a.1.No.Mensaje para registrar primero metodo de pago acorde/pop up o pasar a apartado para registrar metodo de pago directo desde donde estaba / TAMBIEN dejar desabilitado controles de puja

11.1.b.1.b.1.b.1.b.1.b.1.Si.¿Monto dentro de los límites del método de pago usado? (Comprobacion de limites de pago dentro de esto, comprobando antes de cada puja, con ese metodo de pago, que no supere el limite que tenga)

11.1.b.1.b.1.b.1.b.1.b.1.a.1.No.Popup indicando que no se puede superar el límite del método de pago con ninguna puja

11.1.b.1.b.1.b.1.b.1.b.1.b.1.Si.¿Subasta es oro o platino? Si es oro o platino, entonces puja por el producto sin ningun limite de monto mínimo ni máximo (Sólo que sea mayor a la puja anterior) (retenido por 1 min por ej., mientras tenga puja ganadora). PERO si la subasta en si es de otras categorias inferiores, entonces puja por el producto pero con limites de monto máximo y mínimo (retenido por 1 min por ej., mientras tenga puja ganadora)

11.1.b.1.b.1.b.1.b.1.b.1.b.2.¿Puja ganada? (A partir del momento de la puja ganada, quizas con algun popup acorde y demas, se pasaria el proceso al apartado de compras, pero aca lo seguimos aca como para representar como seria el flujo mas completo y demas, como para que quede en general todo mas simplificado, pero bueno, en realidad, al ganar la puja, seguro con algun mensaje/popup acorde y demas, se podria seguir en esa subasta si no termino, para poder pujar por otros articulos o lo que se quiera, o se puede salir para seguir todo el proceso de compra en si, con el resto de pagos, detalles, indicaciones y demas como para completar todo el resto de la compra, pagos y demas por esa puja ganada)

11.1.b.1.b.1.b.1.b.1.b.1.b.2.a.1.Si no se gana la puja, se le mostrara un popup de que alguien lo supero o lo que sea acorde, pero habria que ver que se muestra del ganador, quizas solo su numero de usuario como para tampoco filtrar los datos personales de cada ganador o parrticipante de una subasta al resto de participantes. Y bueno, pierde la puja, y sigue normal con el resto de articulos de la subasta, puede salir de esa subasta, y bueno, tambien puede terminar ahi si ese era el ultimo articulo para pujar de esa subasta.

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.Si.¿Verdaderamente tiene los fondos y no ocurrió ninguna excepción de script? 

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.a.1.No.Multa de 10% antes de poder participar en otra subasta, con un plazo de pago de la multa, del producto y de las comisiones + envio (si aplica), de 72hs (quizás agregar como nuevo pago, o quizás como agregado al monto total de la compra en cuestión por la que se le asigna la multa). Posiblemente tener algún apartado de pagos pendientes, o algo por el estilo

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.a.2.¿Paga tanto la multa como el producto dentro del plazo indicado?

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.a.2.a.1.Si. Entonces sigue el proceso normal, pasando a la eleccion despues en la gestion de la compra, para elegir si quiere retirar o no el producto, pero bueno, este flujo lo seguiremos mas en el flujo sin multa para evitar tanta repeticion. (Si paga al menos el artículo, aunque haya sido multado por no poder hacer el pago inmediato apenas gana su puja, entonces también se le envía el dinero al vendedor de manera acorde)

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.a.2.b.1.No.Se bloquea la cuenta del usuario y se deriva el caso a la justicia



11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.1.Si.Se retira el dinero correspondiente a la puja ganada del producto, indicando de que luego debera ver de pagar envio/retirar y las comisiones acordes en el apartado de compras, esto dentro de x limite de tiempo para que no haya problemas (Si paga al menos el artículo, aunque haya sido multado por no poder hacer el pago inmediato apenas gana su puja, entonces también se le envía el dinero al vendedor de manera acorde)

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.¿Elige retirar producto?  (Dentro de esto, según sea acorde, rellenar datos de contacto, dirección de envío y demás según sea necesario)

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.a.1.Si.¿Paga en tiempo y forma las comisiones?

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.a.1.a.1.Si.¿Retira el producto dentro del plazo estipulado?

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.a.1.a.1.a.1.Si.El producto queda en manos del comprador. Luego, según los métodos de pago registrados y las compras realizadas, se va aumentando la categoría del usuario (quizás por cantidad y límite de métodos de pago, y por cantidad y monto total de las compras realizadas (Una vez retirado, ya no aplicaría el seguro en ese punto y el usuario se haría cargo de todo)También enviar mail con comprobantes, facturas, y demás acordes al comprador para que tenga, y posiblemente mandar alguna notificación y mail al vendedor para que se entere de todo bien

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.a.1.a.1.b.1.No.La empresa se queda tanto con el dinero como con el artículo, y se termina el proceso


11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.a.1.b.1.No.La empresa se queda tanto con el dinero como con el artículo, y se termina el proceso



11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.b.1.No.¿Paga en tiempo y forma el envio y comisiones?

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.b.1.a.1.Si.El producto queda en manos del comprador. Luego, según los métodos de pago registrados y las compras realizadas, se va aumentando la categoría del usuario (quizás por cantidad y límite de métodos de pago, y por cantidad y monto total de las compras realizadas. También enviar mail con comprobantes, facturas, y demás acordes al comprador para que tenga, y posiblemente mandar alguna notificación y mail al vendedor para que se entere de todo bien

11.1.b.1.b.1.b.1.b.1.b.1.b.2.b.1.b.2.b.1.b.1.No.La empresa se queda tanto con el dinero como con el artículo, y se termina el proceso












12.Consignar Bien - Aca seguramente deberia ser como metodos de pago, mis compras y demas, con una tarjeta para cada articulo por el, al menos, se envio como para publicar, por cada uno por el que se mando las fotos, datos y demas, como para poder ver el estado de esto, ciertas acciones y demas acordes. Tambien, posiblemente dentro de esto, otro apartado dedicado a la gestion de cuentas bancarias, que seria donde se puede recibir el dinero de una venta, aca poder ver tarjetas por cada cuenta bancaria agregada, poder quizas editarla, o quizas nada de editar, pero si eliminar y/o agregar nuevas (IMPORTANTE, QUIZAS EN METODOS DE PAGO LO MISMO, QUIZAS NO PODER EDITAR, PERO SI SEGURO AGREGAR Y ELIMINAR, AUNQUE SEA UN BORRADO LOGICO, PERO QUIZAS NO EDITAR PARA MANTENER CIERTA CONSISTENCIA EN LA BD, O QUIZAS VER DE CREAR UN NUEVO METODO DE PAGO POR CADA UNO QUE SE EDITA, COMO QUE AL EDITAR UN METODO DE PAGO, O UNA CUENTA BANCARIA, QUE SEA UNA NUEVA FILA EN SU TABLA ACORDE QUIZAS, PERO REVISAR BIEN ESTO) Y bueno, sobre cada articulo subido, con su tarjeta acorde y demas, se podria entrar a cada uno y ver ciertas cosas, estado, ciertas acciones acordes y demas, como para poliza, comprobacion de origen licito, etc. Y tambien en general como para ir pudiendo ver el ciclo de subirlo a subasta y venderlo (esto si todos los pasos previos van bien obvio). En este sector, una vez que se sube todo bien del proceso del articulo como para poder subastarlo y demas, una vez que esta todo bien, la empresa de subastas propone valor base + comisiones, y demas detalles, bueno, una vez aceptado esto por parte del vendedor para un articulo particular, entonces se le debe enviar algun documento de confirmacion de la consignacion al mail, y ademas de eso, en la tarjeta de ese articulo en este sector, al ver los detalles y demas, debe poder verse ese documento ahi tambien, con los detalles y demas acordes. Y ademas de esto, cuando se confirma el pago de uno de esos articulos del vendedor, tambien se debe enviar otro documento al mail, algo como comprobante/liquidacion de venta, para ese articulo particular, y ademas de eso, dentro del detalle de ese articulo, poder visualizar ese documento en la app, es decir, con todos los detalles de la cuenta donde se pago, valor final, valor base, y demas detalles acordes para que el vendedor este al tanto de todo bien.

12.1.¿Usuario con forma de recibir el pago registrada?

12.1.a.1.No.Pop up indicando la necesidad de esto, como una cuenta bancaria, y se le muestra una ventana allí, o se lo desplaza a una vista diferente para realizar dicho registro

12.1.b.1.Si.¿Usuario con método de pago registrado y verificado?

12.1.b.1.a.1.No.Pop up indicando la necesidad de esto, y se le muestra una ventana allí, o se lo desplaza a una vista diferente para realizar dicho registro

12.1.b.1.b.1.Popup de aceptación de todas las condiciones básicas, tick de confirmación de ownership, hacerse cargo del envío/retirar para devoluciones y demás (Esto quizas aca o despues de que la empresa muestre el interés real)

12.1.b.1.b.2.Registra todos los datos de los articulos a subastar segun el proceso normal

12.1.b.1.b.3.¿Empresa duda del origen?

12.1.b.1.b.3.a.1.Si.Pantalla para ingresar documentos y demás acordes para demostrar origen lícito
12.1.b.1.b.3.a.2.Una vez todos los comprobantes necesarios esten enviados, se le advierte que puede tardar la verificacion, y quizas mandar un email acorde cuando la empresa confirme todo bien

12.1.b.1.b.3.a.3.¿Empresa acepta los comprobantes de origen?

12.1.b.1.b.3.a.3.a.1.Si.¿Empresa interesada inicialmente? (Se puede llegar aca desde despues de que vaya bien toda la revision de origen licito, si se pide, o tambien llegar sin que la empresa de subastas pida esa revision)

12.1.b.1.b.3.a.3.b.1.No.Se muestra un mensaje acorde a la causa, y el proceso finaliza sin envio, venta ni nada

12.1.b.1.b.3.b.1.No.¿Empresa interesada inicialmente?

12.1.b.1.b.3.b.1.a.1.No.Se muestra un mensaje acorde a la causa, y el proceso finaliza sin envio, venta ni nada

12.1.b.1.b.3.b.1.b.1.Si.Se envia el bien a la empresa

12.1.b.1.b.3.b.1.b.2.¿Revisión personal por parte de la empresa satisfactoria? ¿Sin detalles raros, roturas, etc?

12.1.b.1.b.3.b.1.b.2.a.1.No.El proceso finaliza, se devuelve el bien a cargo del potencial vendedor, y se termina sin venta

12.1.b.1.b.3.b.1.b.2.a.2.¿Usuario retira o paga el envío de vuelta acorde dentro del plazo estipulado?

12.1.b.1.b.3.b.1.b.2.a.2.a.1.Si.Ocurre todo normal, tiene su propio artículo y finaliza el proceso ahí (IMPORTANTE, POSIBLEMENTE HACER ALGUNA CONDICIÓN PARA QUE NO PUEDA VOLVER A HACER SUBMIT DEL MISMO ARTÍCULO EN OTRO MOMENTO)

12.1.b.1.b.3.b.1.b.2.a.2.b.1.No.La empresa puede decidir entre quedarse con el artículo, descartarlo, entre otros

12.1.b.1.b.3.b.1.b.2.b.1.Si.¿El usuario acepta el precio base y comisiones propuestas por la empresa?

12.1.b.1.b.3.b.1.b.2.b.1.a.1.No.El proceso finaliza, se devuelve el bien a cargo del potencial vendedor, y se termina sin venta

12.1.b.1.b.3.b.1.b.2.b.1.a.2.¿Usuario retira o paga el envío de vuelta acorde dentro del plazo estipulado?

12.1.b.1.b.3.b.1.b.2.b.1.a.2.a.1.Si.Ocurre todo normal, tiene su propio artículo y finaliza el proceso ahí (IMPORTANTE, POSIBLEMENTE HACER ALGUNA CONDICIÓN PARA QUE NO PUEDA VOLVER A HACER SUBMIT DEL MISMO ARTÍCULO EN OTRO MOMENTO)

12.1.b.1.b.3.b.1.b.2.b.1.a.2.b.1.No.La empresa puede decidir entre quedarse con el artículo, descartarlo, entre otros


12.1.b.1.b.3.b.1.b.2.b.1.b.1.Si.Sistema de asignación de póliza, junto con la actualización de esto. Para el aumento de poliza, es absolutamente todo externo, entonces no hacer nada para esto. Para asignar polizas, si son conjuntas, es decir, para mas de 1 articulo, deben ser necesariamente del mismo vendedor y en la misma subasta. La subasta se le asgina al articulo al confirmar las comisiones y demas, se le notifica al usuario toda la info, comisiones, valor base, subasta en la que se agregara, fecha y demas. Las polizas son al valor base al que se va a subasta el producto














12.1.b.1.b.3.b.1.b.2.b.1.b.2.¿Cantidad de artículos a vender muy numerosa? (Establecer límite, quizás algo tipo 5 o más artículos del mismo usuario

12.1.b.1.b.3.b.1.b.2.b.1.b.2.a.1.No.Se procede al proceso de subasta tradicional, rellenando datos, con póliza de seguro acorde y demás 

12.1.b.1.b.3.b.1.b.2.b.1.b.2.a.2.¿Quedó algún artículo de una subasta por el que nadie pujó? Si pasa esto último, entonces la empresa se hace cargo y adquiere, por su precio base, todos aquellos productos por los que no se ofertó, dentro de una subasta ya finalizada

12.1.b.1.b.3.b.1.b.2.b.1.b.2.a.3.Se envía el dinero a la cuenta del vendedor acorde, con los datos de la cuienta que ya haya registrado


12.1.b.1.b.3.b.1.b.2.b.1.b.2.b.1.Si.(Si la empresa lo ve acorde) Se registran todos los artículos del mismo usuario en una subasta específica para dicho individuo. Y por el resto, se procede al proceso de subasta tradicional, rellenando datos, con póliza de seguro acorde y demás 

12.1.b.1.b.3.b.1.b.2.b.1.b.2.b.2.¿Quedó algún artículo de una subasta por el que nadie pujó?Si pasa esto último, entonces la empresa se hace cargo y adquiere, por su precio base, todos aquellos productos por los que no se ofertó, dentro de una subasta ya finalizada

12.1.b.1.b.3.b.1.b.2.b.1.b.2.b.3.Se envía el dinero a la cuenta del vendedor acorde, con los datos de la cuienta que ya haya registrado

Además de las pantallas principales ya definidas, el flujo de la aplicación contempla ciertos estados, pantallas auxiliares y popups necesarios para garantizar una experiencia de uso completa y coherente. En primer lugar, luego del splash se incorpora una pantalla de selección inicial donde el usuario puede optar por ingresar, registrarse o continuar como invitado, ya que este punto de entrada debe quedar explícito en el recorrido de la app.
También se contempla el manejo del estado de la cuenta del usuario, especialmente en los casos en que el registro aún no fue validado por la empresa, la cuenta fue bloqueada, u otras condiciones por las que el usuario todavía no está habilitado para participar de ciertas funciones, o directamente no puede usar la app en realidad, esto para cuentas bloqueadas o sin todavia aprobacion/validacion, aunque siempre puede entrar como invitado para navegacion general. En estos casos, no se trata necesariamente de una pantalla principal independiente, sino de un estado de acceso o bloqueo que permita informar claramente la situación. Del mismo modo, dentro del registro de dos etapas, se considera el flujo de completar clave luego del mail de confirmación, así como opciones de recuperación y cambio de contraseña, con los mails correspondientes para validar estas acciones.
Por otra parte, el estado de puja activa se resuelve dentro de la vista principal de cada subasta, ya que la sala en vivo forma parte del servicio externo de streaming y no de la app en sí. En esa vista se concentra todo lo relacionado con la participación en la subasta: ver el producto actualmente ofertado, realizar una puja, recibir notificaciones de nuevas ofertas, y mostrar popups o mensajes de estado como puja enviada, esperando confirmación, puja rechazada, puja superada o puja ganada. De esta manera, el circuito de interacción queda agrupado dentro del módulo de la subasta, sin necesidad de crear una pantalla separada para la sala en vivo.
De forma similar, se define un sector único para la gestión de pagos pendientes asociados a cada compra ganada, donde el usuario pueda ver y resolver en un solo lugar todo lo relacionado con esa operación: pago del artículo, comisiones, envío si corresponde, multas pendientes y eventuales devoluciones o reintegros. Esto evita dispersar acciones de una misma compra en distintas pantallas y permite concentrar todo el seguimiento económico en un único flujo.
En el caso de la consignación de bienes, el seguimiento de cada artículo se organiza como una tarjeta o registro individual dentro de una bandeja de solicitudes. Allí se puede visualizar el estado del bien en cada etapa: borrador, enviado, en revisión, aceptado, rechazado, en traslado, inspeccionado, aprobado para subasta, publicado y finalmente vendido o no vendido. Esta organización permite representar con claridad el ciclo completo del proceso de consignación, tanto desde el punto de vista del usuario como desde el control de la empresa.
Además, se considera necesario incluir estados de error y contingencia, como problemas de conexión, carga fallida, reintento de envío de datos o indisponibilidad temporal de alguna función. Estos casos pueden resolverse mediante popups, banners o estados de pantalla según corresponda, ya que forman parte del uso real de la aplicación y deben quedar contemplados desde el diseño.
Por último, también se prevén estados vacíos en los listados principales, por ejemplo cuando no hay subastas disponibles, no existen compras registradas, no hay métodos de pago cargados o todavía no se consignaron bienes. Estos casos deben resolverse con mensajes claros para evitar pantallas vacías sin contexto y guiar al usuario hacia la acción siguiente.

—>Entonces finalmente, tenemos 5 documentos principales, que se deberían poder enviar al mail, y además poder visualizar dentro de la app, cuando apliquen, dentro del detalle de cada apartado acorde:
- Comprobante de compra (Comprador)
- Recibo de multa (Comprador)
- Acuerdo de consignación (Vendedor)
- Liquidación de venta (Vendedor)
- Comprobante de pago de envio de devolución: Se agrega tambien un comprobante de pago para que el usuario reciba cuando paga por el envio para la devolucion de un bien que envio a la casa de subastas pero por alguna razon no se llevo adelante ponerlo en subasta y demas del proceso normal, esto se puede descargar, reenviar y demas como para los otros documentos





En síntesis, el flujo general queda compuesto por pantallas principales, subpantallas de detalle, estados de validación, popups de advertencia y módulos de seguimiento, buscando que cada proceso importante tenga un lugar claro dentro de la navegación y que la lógica funcional de la app esté completamente representada desde el diseño.




















Esto seria el stack al menos de manera general, posiblemente se pueden cambiar/agregar algunas cosas, pero es al menos lo planificado para usar y demas, para backend y frontend:

— Stack Backend:
Java
Spring Boot
Spring Security + JWT
Hibernate / JPA
WebSocket
PostgreSQL
Docker
Flyway
multipart + BLOB
Postman
@controlleradvice para manejo de errores?

— Stack Frontend:

React native
CLI
Typescript
React Navigation
Zustand
Fetch nativo/Axios (Decidir entre estos 2)
Tanstack Query
Websockets
UI custom con StyleSheet
Algunas dependencias/elementos posiblemente necesarios para React Navigation:
react-native-screens y react-native-safe-area-context

