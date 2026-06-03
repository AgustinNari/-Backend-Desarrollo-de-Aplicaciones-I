# Material fuente: banco_preguntas_respuestas_original.md

> Este archivo conserva el texto original recibido para que Codex tenga acceso a todos los detalles, incluso los que no estén resumidos en la documentación central.

---

Banco de preguntas generales, junto con sus respuestas aproximadas:

- Consideracion general para todas las preguntas y respuestas: cuando en algunas respuesdtas indico algo del estilo “como veas acorde” o similar, seria para dejarlo a tu criterio y lo que consideres mejor en cada caso, buscando en general, que resulte en algo solido y que funcione bien de todo de la app, pero sin complicarse demasiado, buscando tambien cierta simplicidad, pero obvio siempre garantizando que funcione bien y se cumpla con todos los requerimientos.
1. Alcance general de la app
1.1. ¿La app será solo para postores/compradores y consignadores, o también tendrá un perfil interno para empleados de la empresa? es solo para eso, no tendra perfil interno para empleados, al menos no en el frontend en si, pero si podremos tener algunos endpoints y metodos internos para hacer ese “poceso de admin” (que seria eso mas manual), pero no hace falta nada de frontend para eso, porque en realidad, se podrian ingresar los datos y demas todo simplemente con comandos basicos SQL, es decir, no es que hace falta nada de modo admin, pero si lo quiero agregar/tener como para que sea mas ameno el proceso de probar todo.
1.2. ¿El modo invitado podrá ver solo subastas y catálogos, o también alguna información adicional como detalle parcial de ítems, historial público o estado general de subastas? A eso que dijiste, de ver subastas y catalogos de manera general, se suma ver el detalle de cada item del catalogo, pero IMPORTANTE, sin poder ver el precio de ninguno de esos items, y dentro de una subasta, no podra ver ni el precio base de cada una, ni cual es el valor que esta siendo subastado, osea, en terminos de precios/valores/montos de dinero, no debe poder ver nada de nada, pero si debe poder ver los detalles generales de una subasta, y los detalles generales de cada articulo del catalogo. Pero no debe poder ver nada del proceso de puja en si. Posiblemente, seria armarlo bastante separado, incluso sin poder ver que articulos fueron subastados o no, ni cual esta siendo subastado en ese momento. Es decir, seria solo poder ver los detalles generales de la subasta, y de los items del catalogo, pero sin precio, y sin ver nada de que esta pasando en vivo en la misma, seria como el acceso a un folleto general de la subasta, sin actualizaciones del progreso en vivo, ni historial de pujas por ej. y sin nada de precios. (Seguramente si se podria ver que esta en ese momento en vivo/iniciada una subasta, pero no se sabria nada mas del estado actual de la misma en si)
1.3. ¿Qué funciones exactas quedan bloqueadas para un invitado y cuáles quedan visibles solo en modo lectura? En general, seria accesible y medio legible, solo el apartado de subastas, con catálogos y demas, pero para el resto de funciones, no serian accesibles, osea quizas se podria presionar algo, y que aparezca la pantalla de “funcion bloqueda por estar como invitado”, o algo por el estilo, y bueno, ademas de eso, deberia poder, quizas en el apartado de perfil, o de menu lateral, algun boton para login/registro, para volver a esa pantalla de seleccion, y poder elegir eso en lugar de ingreso como invitado
1.4. ¿La navegación principal de la app será igual para invitado y usuario autenticado, cambiando solo permisos, o cambiará también la estructura visible del menú? Como te parezca mejor, quizas la primera opcion para ver de lo que se pierden y que los motive a registrarse???
1.5. ¿La app se va a centrar más en la participación en subastas o en el flujo completo de compra, consignación y gestión de cuenta por igual? En general, todos los apartados deben estar perfectamente hechos, pero en terminos de que seria lo que se le da mas importancia en terminos de UI general, seria seguramente las subastas (el gasto de dinero basicamente). Osea, si tuviera que decir un apartado a donde te orienta que vayas la app, seria seguramente el apartado de subastas, al estar su boton en el centro del bottom nav, un poco mas grande, al ser el apartado unicamente accesible como invitado, al ser el apartado donde se lanza la app al estar logueado, etc. Pero igualmente, como ya decia, eso no quita que todos los apartados de la app son importantes, sobre todo los procesos de subastas, finalizacion de compras y consignacion, esos serian seguramente los 3 apartados mas importantes, pero en general todo debe estar bien y solido.
1.6. ¿Se prioriza que todo el flujo sea usable desde mobile sin depender de ninguna parte externa salvo el streaming, o habrá casos donde el usuario deba salir de la app? Pensa que eso del streaming, en realidad no hace falta ni siquiera mencionarlo dentro de mi app, osea, pensalo como algo completamente externo, que ni siquiera hace falta que mi app mencione ni nada por el estilo, entonces, en general, todo lo mencionado de funciones, si, debe ser accesible todo dentro de la app, sin necesidad de salir de la misma, esto salvo para las verificaciones por mail, envio de documentacion por mail (que no afecta al flujo en si en realidad), etc. Pero sacando eso, todo seria dentro de la misma app, y en realidad, el unico flujo afectado, seria como ya dije, ese de registro, al tener que ir al mail, una vez tenemos la cuenta validada, y usar ese enlace de acceso, para entrar con todo bien a la app, y poder completar el proceso de registro.
1.7. ¿Qué partes del sistema existente deben quedar sí o sí integradas desde el inicio y cuáles pueden quedar para una segunda etapa? En general, todo del sistema existente que involucre a algo/tenga relacion con algo de mi app, debería quedar integrado, aunque no sabria bien desde que etapa, eso miralo vos.
1.8. ¿Hay alguna funcionalidad que deba quedar explícitamente fuera de la primera versión aunque esté contemplada a futuro? En general no, me gustaria poder tenerlo todo bien en esta primera version, como para avanzar a un buen ritmo, garantizando que este todo bien y solido.

2. Roles, permisos y estados de usuario
2.1. ¿Cuáles son exactamente los roles o tipos de usuario que existirá en la app? Para usuarios espeicficos de la app, es decir, descartando posibles usuarios del sistema ya existente, u otro tipo de usuarios/personas registradas en la BD, descartando eso, diria que no tendria roles de nada, porque si es un usuario invitado, entonces no guardaria nada en la BD. Basicamente, para los usuarios que usarian la app, no habria roles de nada, lo que si, es que quizas podria haber alguna diferenciacion en lo que se guarda en la BD de los empleados, para el sistema ya existente, pero no seria algo de verdad propio de mi app, sino quizas algo del posible backend del sistema ya existente, con el que se podria conectar mi app.
2.2. ¿El usuario normal puede también consignar bienes, o hay usuarios que solo pueden pujar y otros que solo pueden consignar? Si es un usuario registrado y validado, sin restricciones por multas, entonces podra hacer todo, siguiendo los pasos y requisitos para cada uno de esos procesos obviamente, pero en general, no habria ninguna diferenciacion predefinida para eso (como roles definidos de vendedor/comprador, no habria nada de eso), salvo las excepciones mencionadas (por ej, un usuario con multa pendiente de pago).
2.3. ¿Existe diferencia funcional entre usuario registrado, usuario verificado, usuario aprobado y usuario bloqueado? Si, como ya aclare a lo largo de la info enviada, un usuario con bloqueo de pujas (por multa pendiente), o un usuario con bloque permanente, obviamente hay muchas cosas que no podrán hacer en terminos de las funciones de la app. El de bloque permanente literalmente sin acceso a ninguna de las funciones. Y para la comparación entre usuario solo registrado, y usuario validado/verificado/aprobado por la empresa, el solo registrado (sin validacion de la empresa todavia), no podra hacer nada hasta que no este validado, osea, no podra ni siquiera entrar al apartado normal de la app, unicamente intentar loguearse, pero si no esta validado, entonces eso no llevara a nada, y para entrar al menos de cierta manera a la app, antes de ser validado, lo unico que podra hacer es usar el modo invitado.
2.4. ¿La categoría del usuario se asigna solo manualmente al registrarse o también puede cambiar automáticamente por actividad? Se asigna por parte de la empresa al validar al usuario, osea es algo manual, y despues, sin importar la categoria con la que se inicia, se podra sumar o restar puntos, y segun eso, se ira subiendo o bajando de categoria, y esos puntos seran segun la actividad que tenga, pujas realizadas, compras concretadas, pagos no completados correctamente, etc.
2.5. ¿La categoría puede subir y bajar con el tiempo, o solo subir? Puede subir y bajar
2.6. ¿Qué acciones concretas otorgan puntos para subir de categoría? Seria hacer pujas, tener pujas que terminen siendo ganadoras, concretar esas compras despues de una puja ganadora y tambien publicar articulos para consignar y que terminen siendo subastados en si (osea no solo publicar cualquier cosa, sino que se sumarian los puntos recien cuando la empresa confirma todo de ese producto, ya fue recibido, se hace todo el proceso, se acepta el acuerdo y se le asigna la subasta donde se podra pujar por ese articulo, recien ahi se sumarian esos puntos por consignacion). Tambien, se sumarian puntos al registrar nuevos metodos de pago, pero unicamente al momento de ser validados por la empresa, es decir, no antes, porque sino seria solo ingresar cualquier cosa y listo para ganar puntos infinitos.
2.7. ¿Qué acciones restan puntos o perjudican la categoría? Esas acciones serian principalmente, que al momento de ganar una puja, y se intentaria realizar el pago en si de lo pujado, que ocurra una excepcion con el metodo de pago, y no se concrete el pago en ese momento, lo que generara una multa, osea, por cada situacion que genere una multa, entonces se restaran puntos, y bueno ademas, cuando no se paga esa multa en plazo, que eso en una parte genera un bloqueo por tiempo indeterminado de la cuenta, pero aparte de eso, seguramente restar puntos tambien por ese suceso. Osea, la resta de puntos seria en general alrededor de las multas generadas.
2.8. ¿La empresa puede modificar manualmente la categoría del usuario aunque el sistema de puntos diga otra cosa? Posiblemente si, quizas podria alguien con un endpoint en backend hacerlo, pero tampoco es algo tan central de la app. Si se quiere hacer, seria seguramente con en endpoint auxiliar de ese modo admin, para poder establecer los puntos de cualquier usuario, y que eso desencadene en el cambio de categoria acorde.
2.9. ¿Qué pasa con un usuario que tiene múltiples sanciones, multas o pagos vencidos? Se aplica todo segun las reglas ya dichas, para pagos no realizados, se genera la multa, para multas no pagas, se genera ese bloqueo de tiempo indefinido/permanente.
2.10. ¿La cuenta bloqueada significa bloqueo total o solo bloqueo de funciones económicas y de subasta? Bloqueo total, sin poder navegar por UI ni nada, seria seguramente iniciar sesion, y directo la pantalla de info de cuenta bloqueada, que hacer y demas.
2.11. ¿Hay un estado intermedio entre “bloqueado” y “pendiente de verificación”? Seguramente no, osea serian medio de diferentes flujos igual. Bloqueado cuando no paga una multa en plazo, y en ese estado, solo seria posible un arreglo manual por parte de la empresa. Y pendiente de verificacion, entiendo que seria seguramente una vez termina las primeras etapas del registro, que hace falta la validacion manual de la empresa.
2.12. ¿Un usuario suspendido puede seguir viendo la app como invitado o queda directamente sin acceso? Directamente sin acceso, puede desloguearse para entrar como invitado, pero nada mas que eso, solo podria ver esa pantalla que ya dijimos de info general del estado de la cuenta/estado del bloqueo.

3. Registro, onboarding y autenticación
3.1. ¿El registro de dos etapas es obligatorio para todos los usuarios o hay casos especiales? obligatorio para todos
3.2. ¿En la etapa 1 se valida solo formato de datos o también ya se detectan duplicados y restricciones? en general diria de validar todo
3.3. ¿El domicilio legal debe guardarse como texto libre o estructurado por campos? como veas acorde
3.4. ¿El país de origen se elige desde un listado fijo basado en la tabla de países? si, entiendo que se pondrían seguramente todos los paises como opción
3.5. ¿La foto del documento debe aceptarse como imagen, PDF o ambos? imagen, seguramente pudiendo meter una imagen, o sacar foto con la camara, pero seguramente nada de pdf
3.6. ¿La etapa 2 con fotos del DNI puede hacerse desde galería, cámara o ambas? seguramente ambas
3.7. ¿La validación de la etapa 1 y 2 es manual, automática o híbrida? entre esas etapas, seria todo automatico del sistema, pero una vez que completa la subida del paso 2, de subir las imagenes del DNI, entonces despues de eso, la validacion seria manual
3.8. ¿Cuánto tiempo puede pasar entre la etapa 1 y la etapa 2 antes de caducar el registro incompleto? como veas acorde, no se si pondria limite
3.9. ¿Qué ocurre si el usuario vuelve a abrir la app después de haber iniciado el registro y no lo terminó? si no la cerro, entonces entiendo que deberia quedar guardado, si simplemente cambio a otra cosa del celular y volvio, pero si cerro la app, entonces seguramente se perderia el progreso que haya hecho, y se deberia volver a empezar, todo hasta llegar a terminar el paso 2, para que despues quede en manos de la empresa
3.10. ¿El token de verificación enviado por mail tiene un solo uso o puede reutilizarse dentro del plazo? 1 solo uso por cada uno, y con plazo de vigencia temporal, osea seria ambas condiciones que se deben cumplir para que sea valido.
3.11. ¿La creación de clave final depende solo del token de mail o también de un enlace directo en el correo? como veas acorde, pero seria basicamente ir al mail recibido, presionar el enlace, y que eso mande a la app, y abrirla en la pantalla acorde para completar con la contraseña nueva a crear, y asi completar el registro.
3.12. ¿La contraseña tiene requisitos mínimos fijos? ¿Cuáles? si, en general lo basico, cantidad de caracteres (8 por ej), numeros, y algun simbolo especial quizas, pero como veas acorde
3.13. ¿El login será solo con email y contraseña o se prevén otros métodos a futuro?solo con eso
3.14. ¿La recuperación de contraseña tiene un límite de intentos por IP, por mail o por usuario?no creo que tenga limite, seria seguramente mandar el mail, que use ese enlace y listo (lo que si, el enlace tendria como para el de registro, un unico uso y limite de tiempo)
3.15. ¿Se permite cambiar contraseña desde sesión activa además de recuperación por mail?si, y ademas, estando en sesion activa, seria sin necesidad de mandar un mail con enlace
3.16. ¿El token de sesión expira por tiempo fijo o por inactividad? quizas por tiempo fijo, pero como veas acorde
3.17. ¿Se requiere refresh token o solo access token? como veas acorde
3.18. ¿La app debe recordar sesión al reiniciarse o pedir login de nuevo? si se cierra y vuelve a abrir, deberia guardar la sesion iniciada, osea, no deberia pedir de vuelta login, si no se hace logout y se cierra y abre la app.
3.19. ¿Cómo se maneja el reenvío del enlace de registro pendiente si el usuario ya fue aprobado pero nunca completó la clave?se ingresa el mail en el apartado acorde dentro del registro, y si el usuario de verdad ya esta validado, se enviara el mail con enlace acorde para completar todo bien, pero si no esta validado, no esta registrado, u otros, entonces no se enviara nada al meter ese mail, pero tampoco aclarar cual es el estado de ese mail especifico en la UI de la app, porque sino cualquier usuario podria poner mails de otros, ajenos, y podria ver si tiene cuenta registrada, si esta validad, etc..
3.20. ¿Se necesitan pantallas de error específicas para email inválido, cuenta pendiente y token expirado, o basta con un mensaje genérico? seguramente mensaje y listo

4. Perfil, categoría y reputación
4.1. ¿Qué datos exactos debe mostrar la pantalla de perfil? seguramente botones para historial, para estadisticas, para metodos de pago, y para cambio de contraseña y para gestion de direccion o quizas hasta varias direcciones de envio (que podria ser diferente al domicilio legal)
4.2. ¿Qué campos del perfil son editables por el usuario y cuáles son solo informativos? Lo unico editable, al menos por ahora, dentro de esas cosas de perfil, seria poder agregar/eliminar metodos de pago, cambio de contraseña, y esa gestion de direccion/es de envio.
4.3. ¿La dirección del perfil es la misma que la usada para envío de compras o pueden existir varias direcciones? podria ser diferente entre la de domicilio legal y las de direcciones de envio, pero bueno, quizas poner una unica para la direccion de envio, como veas acorde. Es decir, con esto ultimo, quedarian unicamente 2 direcciones posibles, el domicilio legal que no se usaria mucho creo, y despues, lo editable, que seria la direccion de envio que se quiera usar (que puede ser la misma o diferente de la del domicilio legal)
4.4. ¿La categoría visible del usuario será solo una etiqueta o también se mostrará el puntaje de progreso? ambos, etiqueta de la categoria puntual, y tambien los puntos y progreso hacia la siguiente
4.5. ¿El sistema de puntos será visible para el usuario o solo el resultado final de categoría?si, vera ambos
4.6. ¿Qué reglas exactas determinan pasar de común a especial, de especial a plata, etc.? por cantidad de puntos segun cada escalon y listo, si llega a x puntos totales, entonces va subiendo de categoria, cada categoria definida en x cantidad de puntos acumulados/totales.
4.7. ¿La asignación inicial de categoría queda siempre a criterio de la empresa? si
4.8. ¿El usuario puede ver el motivo de por qué tiene una categoría determinada? seguramente no
4.9. ¿Se mostrará un historial de cambios de categoría o solo el estado actual? solo estado actual, aunque tendria cierta info, al menos relacionada de cierta manera, en el apartado de estadisticas seguramente.
4.10. ¿Qué datos de reputación o actividad se consideran útiles para mostrar en el perfil? como ya dijimos, eso de los botones para las pantallas de historial y de estadisticas

5. Métodos de pago
5.1. ¿Qué tipos de medios de pago estarán realmente soportados en la primera versión: tarjetas, cuentas bancarias, cheques o todos? solo esos 3
5.2. ¿Qué diferencias concretas hay entre un medio de pago “registrado” y uno “verificado”? el registrado no se puede usar para nada, unicamente se puede usar cuando esta verificado/validado por la empresa
5.3. ¿La verificación de medios de pago es manual, automática o ambas? manual
5.4. ¿El usuario puede tener varios medios verificados al mismo tiempo? si
5.5. ¿Existe un medio principal por defecto para pujas y pagos? el usuario podria definir uno de sus medios como el “principal”, pero podria no ser aplicable para todo, porque quizas ese esta en pesos, y quiere entrar a una subasta en dolares, entonces no podria usar su metodo de pago principal para pujar en esa subasta
5.6. ¿Se puede cambiar el medio principal aunque esté asociado a una operación pendiente? si, es una condicion en general mas que nada para ui/ux, como para simplificar un poco el uso general, pero no mucho mas que eso
5.7. ¿Se permite editar un medio de pago existente o solo agregar y eliminar? segurametne solo agregar y eliminar (borrado logico)
5.8. Si se edita un medio, ¿se considera un cambio sobre el mismo registro o se crea uno nuevo? seguramente no permitir editar, solo ese cambio de principal o no
5.9. ¿La eliminación es lógica siempre o puede haber borrado definitivo en algunos casos? siempre logica, para que no queden relaciones rotas
5.10. ¿Qué datos exactos se guardan para una tarjeta? los ya mencionados a lo largo de todo lo enviado
5.11. ¿Qué datos exactos se guardan para una cuenta bancaria? los ya mencionados a lo largo de todo lo enviado
5.12. ¿Qué datos exactos se guardan para un cheque certificado? los ya mencionados a lo largo de todo lo enviado
5.13. ¿Qué documentos o imágenes se piden para validar un cheque? fotos del frente y del dorso
5.14. ¿Los cheques necesitan frente y dorso o algún otro respaldo?con frente y dorso ya va bien
5.15. ¿Qué monedas se admiten para cada tipo de medio de pago? pesos argentinos o dolares estadounidenses para cada metodo de pago
5.16. ¿Un medio de pago en USD puede usarse en cualquier subasta en USD o necesita validación específica para cada subasta? si ese metodo de pago esta con una verificacion/validacion vigente en terminos temporales (osea que fue validado y esa validacion todavia no expiro), entonces, ese metodo de pago podra usarse en cualquier subasta en USD (siempre respetando el resto de condiciones generales, como el de categorias segun usuario y subasta)
5.17. ¿Qué significa exactamente que una cuenta bancaria sea “extranjera” dentro de la app? seguramente simplemente una condicion, pero no implicaria mucho
5.18. ¿Cómo se controlan los límites de monto del medio de pago? al validar el metodo de pago, se establece ese limite, y  se guarda cuanto tiene consumido en x plazo de ese metodo, segun sea acorde obvio, porque un cheque no tendria plazos, sino que una vez se gasta algo, eso no se reinicia al mes siguiente por ej. Entonces, se va guardando cuanto va gastando, y se controla que lo que va gastando en la app, pujas y demas, que con lo acumulado, no supere el limite de ese metodo de pago usado.
5.19. ¿Ese límite es por operación, por día, por subasta o acumulado? seria un limite acumulado, que las acciones que haga, no superen el limite que tenga, pero para tarjetas por ej, seria seguramente un reinicio por mes.
5.20. ¿Los fondos reservados de una cuenta bancaria funcionan como bloqueo temporal de saldo? no se bien eso, como veas acorde
5.21. ¿Un cheque certificado funciona como garantía fija hasta su monto total? no se bien eso, como veas acorde
5.22. ¿Cómo se maneja el vencimiento de un medio de pago? seria algo manual, si esta vencido, entonces cuando se pida la validacion, la empresa no lo concede y listo, seria mas que nada manual.
5.23. ¿Se notifica por mail cuando un medio de pago queda aprobado o rechazado? como veas acorde
5.24. ¿El usuario puede usar un medio de pago recién registrado antes de que esté verificado? no
5.25. ¿Hay un plazo de vigencia para la verificación de un medio de pago? si, como veas acorde, pero pensaba por ej, 3, o quizas 5 dias habiles.
5.26. ¿Ese plazo se reinicia con cada nueva validación o es fijo por medio? con cada nueva validacion manual que conceda la empresa, entonces se reinicia ese plazo.

6. Subastas: listado, detalle y catálogo
6.1. ¿La pantalla de subastas mostrará primero activas y después futuras, o también cerradas para consulta? seguramente solo activas y futuras
6.2. ¿El usuario puede filtrar por categoría, segmento, moneda, ubicación, fecha o estado? en general como veas acorde, por ahora pensaba solo categoria, segmento y moneda, quizas estado, pero como te parezca bien
6.3. ¿Qué información debe verse en la tarjeta resumida de cada subasta? lo ya establecido, pero como veas acorde
6.4. ¿Al tocar la tarjeta se entra al detalle de subasta o directamente a la puja actual? primero al detalle de subasta
6.5. ¿El botón de catálogo debe ser separado o accesible desde el detalle principal? accesible desde el detalle de subasta
6.6. ¿El catálogo de una subasta debe ser público para invitados con datos ocultos o completamente visible en modo invitado? en modo invitado, se ve todo menos los estados de cada articulo (no mostrar nada de subastado, en puja activa, proximo a subastar), y tampoco verian nada de valores/precios.
6.7. ¿Qué datos ve un invitado sobre cada ítem y cuáles ve un usuario registrado? lo ya dicho, invitado ve todo menos lo ya dicho.
6.8. ¿El precio base del ítem se oculta a invitados siempre o también en algunas vistas parciales? se oculta siempre a invitados
6.9. ¿El catálogo se verá secuencialmente o con navegación libre por lotes? quizas una lista
6.10. ¿Cada ítem del catálogo muestra solo datos básicos o también pujas pasadas y estado del lote? depende de la vista, para invitado nada de estado, pujas ni nada, pero para registrado, como veas acorde
6.11. ¿Los ítems pueden tener subítems o piezas agrupadas? esto seria segun la consigna
6.12. ¿Cómo se muestra un conjunto de piezas como un juego o lote compuesto? seria seguramente como un juego, pero en la practica, quedaria seguramente como cualquiier otro item general, no habria tanta diferenciacion necesaria entiendo, simplemente se indicaria las partes que lo componen y no mucho mas, pero seguramente no haria falta almacenar cosas por separado, items extras, etc. sino guardarlo como el resto de items, solo que en los detalles, tener las aclaraciones pertinentes.
6.13. ¿Qué diferencias hay en la ficha de un ítem normal y una obra de arte o diseñador? esto seria segun la consigna
6.14. ¿Qué datos extra son obligatorios para obra de arte o diseñador? esto seria segun la consigna, pero en general como veas acorde
6.15. ¿Se muestran historia, autor, fecha y contexto siempre o solo si existen? seguramente solo si existen
6.16. ¿Las imágenes del ítem se cargan en galería, carrusel o formato mixto? dependeria de donde se este viendo, pero para el detalle de item, seria en formato deslizable, osea entiendo que eso implica tipo carrusel
6.17. ¿Cuántas imágenes mínimas y máximas se esperan por ítem? minimo 6 por cada item, maximo como veas acorde, quizas 15 o tipo 20 max
6.18. ¿El catálogo incluye historial de ofertas o solo estado actual y descripción? solo estado actual y alguna otra info basica de cada item, pero sin historial de ofertas en el listado en si de items, sin sobrecargar mucho cada tarjeta de cada item en el catalogo, pero en general, segun lo ya descrito en la info enviada
6.19. ¿La subasta tiene capacidad de asistentes visible al usuario? entiendo que no
6.20. ¿Se necesita mostrar ubicación exacta, rematador, fecha, hora y segmento siempre? seguramente si, en el detalle de la subasta al menos

7. Acceso a subasta y reglas de participación
7.1. ¿El usuario debe estar autenticado para entrar al detalle de una subasta, aunque no pueda pujar? siendo invitado tambien podria, pero con las restricciones de ver menos cosas, no poder pujar y demas ya mencionadas.
7.2. ¿Qué significa exactamente “puede ver la subasta pero no participar” en la experiencia de la app? que puede ver todo de como va la subasta, el estado, pujas y demas, pero no puede pujar en si, osea puede hacer literalmente todo, menos introducir/efectuar una puja en ninguno de los items que se subasten.
7.3. ¿La inscripción previa es obligatoria solo para subastas futuras o también para algunas subastas activas? la inscripcion previa en general, seria unicamente para poder tener un metodo de pago validado, quizas se podrian hacer algunas cosas extra, como mandar mail cuando quede poco para que arranque, alguna notificacion in-app acorde, pero esa inscripcion en si, no implica nada, seria unicamente para que le llegue a la empresa, valide el metodo de pago usado para esa inscripcion, y que una vez hecho eso, el usuario pueda usar ese metodo de pago en cualquier subasta que aplique, pero no es que esta restringido unicamente a las que se haya inscrito previamente. Una vez validado ese metodo de pago, podria entrar a subastas ya arrancadas por ej.
7.4. ¿La inscripción puede hacerse hasta 30 minutos antes, o existe otro margen real? se puede poner un limite de 30 min por ej, antes del arranque, para esa inscripcion y por ende esa solicitud de validacion de un metodo de pago.
7.5. ¿Qué datos se almacenan al inscribirse a una subasta? lo que veas acorde, pero no seria como una participacion en la misma, porque una participacion se definiria seguramente por pujar en algun item de una subasta, eso de inscripciones podria ser otra tabla por ej, que tambien pueda servir para la empresa con cierta info, poder hacer esos envios de mail y/o notificaciones posiblemente, entre otros. Pero en general, como veas acorde
7.6. ¿La inscripción queda pendiente hasta validación manual del medio de pago? puede ser si, osea que tenga algunos estados, y hasta que no se valide, que no se concrete en si esa inscripcion, como para por ej., no mandar emails y/o notificaciones por inscripciones con metodos de pago que finalmente fueron rechazados en el proceso de validacion/verificacion manual.
7.7. ¿El usuario puede inscribirse a varias subastas futuras al mismo tiempo? seguramente si
7.8. ¿Puede estar inscripto a varias subastas, pero conectado en vivo solo a una? exacto, osea solo poder tener una puja activa/ganadora en una al mismo tiempo.
7.9. ¿Qué pasa si el usuario intenta entrar a una subasta sin medio verificado? seguramente se le podria permitir, posiblemente dar un aviso de que no podra participar/pujar, y al intentar pujar, se bloquean los botones de puja y listo.
7.10. ¿Se bloquea todo acceso o solo la acción de pujar? seguramente solo bloquear la accion de pujar
7.11. ¿Qué pasa si la categoría del usuario es menor que la de la subasta? seguramente lo mismo, poder hacer todo, menos la accion de pujar
7.12. ¿Qué pasa si la moneda del medio de pago no coincide con la de la subasta? el usuario podra elegir entre sus metodos de pago registrados al ir a pujar por algun item, pero bueno, si ninguno es de esa moneda acorde de la subasta, entonces ese boton de puja no quedara habilitado con ninguna de esas opciones de metodo de pago (o si no estan validados)
7.13. ¿Qué pasa si el usuario ya tiene una puja ganadora activa en otra subasta? no se le debe permitir pujar en otra subasta, aunque bueno, si esta en el mismo celular, entonces eso no debería ser posible, a menos que quizas cierre y vuelva a entrar, o algun truco del estilo; esto ultimo basicamente porque mientras tenga una puja ganadora, se rentendria al usuario 1 min hasta que gane en si, o hasta que otra persona supere esa puja.
7.14. ¿La conexión a una subasta en vivo reserva una “sesión” exclusiva? seguramente nada de eso, y bueno, pensa tambien que eso de ver las subastas en vivo, no seria nada de mi app, entonces seguramente nada de eso. Y en terminos de entrar, salir y demas, eso lo podria hacer en general libremente, sin que quede registros ni nada, sin restricciones (a menos que tenga puja ganadora activa obvio)
7.15. ¿Cuándo se libera esa sesión exclusiva? nada de esto
7.16. ¿El usuario puede abandonar la subasta y volver a entrar después?  si no tiene puja ganadora activa, entonces si, podra salir, volver a entrar, y lo que quiera

8. Pujas y validación en tiempo real
8.1. ¿La puja se hace sobre el ítem actual o sobre cualquier ítem del catálogo? solo se puede pujar sobre el item actual
8.2. ¿La app muestra solo el ítem actual en vivo o también próximos lotes y pujas anteriores? depende de la pantalla, en el catalogo se puede ver el detalle de cualquier item, pero en la pantalla de item actual/puja en vivo, se mostraria unicamente ese item que se esta pujando en ese momento, con las acciones acordes, algunos datos y nada de los otros items.
8.3. ¿El usuario puede pujar por debajo del mínimo, por encima del máximo o fuera de rango? nada de eso, esto es segun lo ya enviado de info
8.4. ¿Los límites mínimo y máximo dependen del precio base del ítem? si, al ser porcentajes
8.5. ¿En subastas oro y platino los límites no aplican totalmente o solo se flexibilizan? no aplican de ninguna manera, no hay limites minimos ni maximos de puja, siempre que supere la ultima mejor puja obvio (o que sea el precio base si es la 1ra puja)
8.6. ¿El incremento mínimo es siempre el 1% del valor base? si
8.7. ¿El incremento máximo es siempre el 20% del valor base? si
8.8. ¿El usuario ve el monto mínimo sugerido antes de confirmar? puede ser si, como veas acorde
8.9. ¿El usuario ve también el máximo permitido antes de confirmar? puede ser si, como veas acorde
8.10. ¿La puja se bloquea localmente mientras espera confirmación del backend? entiendo que seguramente si, como veas acorde
8.11. ¿Qué pasa si dos usuarios pujan casi al mismo tiempo? siempre debera priorizarse el primero de esos, en caso de que tengan el mismo monto pujado obvio, y si uno supera al otro, simplemente prevalecera la puja mayor obviamente, pero con websockets, entiendo que no deberia haber problemas con eso
8.12. ¿Cómo se resuelve un conflicto de pujas simultáneas? como veas acorde, pero con websockets entiendo que no deberia pasar
8.13. ¿La app muestra el número de postor ganador de forma anónima? seguramente simplemente mostrar el numero si, sin nombre/mail u otros datos personales, simplemente seria el numero de los postores, en eso de historial de pujas, postor con puja ganadora, ganador, etc.
8.14. ¿Se muestran las pujas recientes a todos los conectados? seguramente si (bueno a menos que sea invitado obvio)
8.15. ¿La puja puede quedar “pendiente” hasta confirmación o se confirma siempre de inmediato? como veas acorde, pero entiendo que deberia poder ser todo en ese mismo momento, como mucho, un par de segundos de proceso, o algo de eso entiendo que seria lo maximo aceptable con websockets y demas
8.16. ¿Qué pasa si la conexión se corta justo después de enviar una puja? no se, como veas acorde
8.17. ¿Qué pasa si la puja fue aceptada en backend pero la app no recibió la respuesta? no se, como veas acorde
8.18. ¿Se necesita un mecanismo de reintento seguro para no duplicar pujas? no se, como veas acorde, pero no creo o si, va en general todo esto con websockets entiendo que deberia andar bien, algo simple y funcional ya estaria bien, tampoco volverse locos con tanto detalle/casos extremadamente excepcionales.
8.19. ¿La app debe impedir una segunda puja hasta tener confirmación de la anterior? seguramente si, prevenir eso
8.20. ¿Qué estados exactos debe mostrar la experiencia de puja: enviada, ganadora, superada, rechazada, conflictiva, fuera de rango? puede ser si, como veas acorde
8.21. ¿La pantalla de puja actual debe mostrar temporizador, historial y oferta líder en vivo? esto segun lo ya enviado de info, pero en general si, esos datos, y algunos mas ya dados en esa info
8.22. ¿La puja activa se cierra automáticamente al terminar la subasta o por timeout del lote? si es por un articulo antes del final, entonces simplemente pasa el tiempo sin que puje nadie, o si pujo alguien, hasta que se cumpla 1 min sin que nadie lo dispute, entonces ese seria el ganador, se cierra eso, y se pasa directo a puja activa del siguiente item de la lista. Y si es el ultimo item de la subasta, lo mismo, pero despues de esos tiempos, la subasta termina y listo, todo normal.
8.23. ¿Qué ocurre cuando nadie supera una oferta y el usuario pasa a ser ganador final? se lanza el mensaje acorde, pasa a la pantall de puja ganada, pudiendo ir a pagar en el apartado de compras, o voler a la subasta para ir a pujar por el siguiente item. Y bueno, tambien pueden estar las excepciones esas de metodos de pago en esa pantalla de puja ganada, donde puede pasar que se gane la puja, pero el pago en si, en ese mismo momento, no se concrete (por ej, si hubo algun gasto reciente de la tarjeta, por ej, alguien uso la tarjeta en otro lugar dentro del plazo de validacion/verificacion de la tarjeta, por lo que el limite real en verdad seria menor), y entonces se generaria la multa acorde y demas, por lo que no podria seguir pujando en esa subasta o ninguna otra hasta solucionar eso.
8.24. ¿Qué ocurre si nadie puja por un ítem? se pasa al siguiente normal en eso de puja activa, y la empresa en si sera la que compre el item por el precio base.
8.25. ¿La empresa compra el ítem al precio base en ese caso, o solo en algunos escenarios? en todos los casos en que nadie puje por un item, entonces la empresa lo compra al precio base

9. Post-puja, ganancia, pago y penalizaciones
9.1. ¿Qué ocurre inmediatamente después de ganar una puja? En ese mismo momento, se intenta realizar el pago, y a menos que ocurra alguna de las excepciones ya mencionadas con el metodo de pago, entonces se hara el pago y demas todo bien, se pasa a una pantalla de puja ganada, y ahi se le da la opcion de ir a compras para completar pago de comisiones + envio (si aplica), y demas detalles, o si no quiere eso, puede seguir en esa misma subasta para poder pujar por el nuevo item activo/en puja. Igualmente, IMPORTANTE, eso de las excepciones de metodo de pago, seria en realidad seguramente algo simulado, quizas algun random o algo por el estilo, que tenga x probabilidad de pasar, porque en realidad, no tenemos un modulo de pagos de verdad conectado, sino que son pagos simulados digamos, entonces básicamente, si no se lo configura como posibilidad en el codigo (de la manera que sea), esas excepciones de no poder pagar una puja ganada al momento y por ende generar una multa, entonces no podrian pasar basicamente.
9.2. ¿El usuario pasa automáticamente al módulo de compras o recibe primero un resumen de adjudicación? primero ese resumen, con posibilidad de ir a compras o seguir en la misma subasta
9.3. ¿Qué datos se muestran en ese resumen inicial? lo ya dicho en la info enviada, pero como veas acorde
9.4. ¿El pago del artículo es inmediato o puede diferirse dentro de un plazo? es inmediato, y si no se puede hacer por una excepcion, entonces se genera la multa. Pero eso solo para el valor pujado, porque las comisiones, envio, y demas se pagan despues dentro de un plazo.
9.5. ¿Qué pasa si el usuario no tiene fondos suficientes al ganar? Si pasa eso, seria porque ocurrio una excepcion de las mencionadas, y se le generara una multa. Esto tambien porque se va controlando los gastos que sean dentro de la app con los limites ya mencionados, entonces, a menos que sea una excepcion externa de algun tipo, no existe la posibilidad de no tener fondos suficientes, ya que los limites de la pagina no lo permiten, al poder ir viendo lo que gasta en si en la app de subastas.
9.6. ¿La multa del 10% se genera automáticamente? si
9.7. ¿La multa se paga por separado o se agrega al mismo flujo de compra? en general esta dentro del mismo flujo/apartado, haciendo un pago especifico para el monto de puja ganadora + multa, con recibo de multa, y despues de eso, otro pago para comisiones + envio y demas. 
9.8. ¿El usuario puede ver una compra con multa pendiente dentro de “Mis compras”? si
9.9. ¿La multa bloquea nuevas subastas hasta que se pague? si
9.10. ¿El plazo de 72 horas aplica para la multa, para el artículo o para ambos? osea el pago unicamente se puede hacer en conjunto, entonces seria medio que para ambos, ya que no se puede pagar la multa o el articulo solo, sino que seria un unico pago de todo junto.
9.11. ¿Qué ocurre si no paga dentro de esas 72 horas? se bloquea la cuenta de manera indefinida/permanente
9.12. ¿La derivación a justicia implica cierre total de cuenta? en parte si, pero seguramente seria reversible de manera manual por un empleado de la empresa, solo que la empresa tendria libertad de hacer lo que quiera. Pero en terminos de funciones si, no podria hacer nada mas en la app, exceptuando hacer login y ver una pantalla general de su situacion.
9.13. ¿Se pueden pagar artículo, multa, comisión y envío en un mismo checkout o en pasos separados? serian 2 pasos separados, 1 para articulo + multa, y otro para comision + envio, que si no hay multa, ese segundo seria lo unico necesario de pagos para hacer en el apartado de compras.
9.14. ¿La comisión siempre existe o depende del tipo de bien o subasta? seguramente si, pero variaria segun lo que vea acorde la empresa
9.15. ¿El envío se cobra siempre al comprador o puede ser opcional por retiro personal? estan esas 2 opciones, pero si quiere envio, eso siempre se cobra
9.16. ¿Qué datos se piden para el envío a domicilio? esta mas o menos en la info ya enviada
9.17. ¿El usuario puede elegir retiro en sucursal en lugar de envío? si
9.18. ¿La cobertura de seguro se pierde si retira personalmente? si, osea el seguro seria hasta que el comprador tenga el articulo en su posesion
9.19. ¿Qué pasa si una compra ya está pagada y luego cambia la modalidad de entrega? si ya pago comisiones + envio (si aplica), entonces no puede cambiar anda de la entrega
9.20. ¿Qué validaciones se hacen antes de aceptar el pago final? como veas acorde
9.21. ¿Qué pasa si el medio de pago falla durante el cobro? si es del pago de comisiones + envio, entonces nada, simplemente no se realiza, y seguira teniendo que pagar.
9.22. ¿Qué pasa si el pago fue confirmado en backend pero la app no recibió respuesta? no se, como veas acorde

10. Compras, documentos y comprobantes
10.1. ¿Qué estados exactos tendrá una compra? como veas acorde
10.2. ¿Qué tabs o filtros se mostrarán en “Mis compras”? en general esta en la info enviada, pero como veas acorde
10.3. ¿Una compra puede estar pendiente de pago, pagada, con multa, con envío pendiente o con devolución pendiente? devolucion de compra nada (osea por algo comprado, no existe devolucion), pero los otros puede ser, como veas acorde
10.4. ¿Qué debe verse en la tarjeta resumen de cada compra? esta medio en la info enviada, pero como veas acorde
10.5. ¿Qué debe verse dentro del detalle completo de una compra? esta medio en la info enviada, pero como veas acorde
10.6. ¿La factura de adjudicación se emite solo cuando se paga la compra completa? seguramente si
10.7. ¿El recibo de multa se emite apenas se registra la multa o solo cuando se paga? cuando se paga entiendo
10.8. ¿Qué documentos debe poder ver y descargar el comprador desde la app? los 2 ya mencionados en la info enviada para el proceso de comprador, y los otros 3 para el de consignacion, osea 5 serian en general de toda la app, de lo ya enviado.
10.9. ¿Los documentos se adjuntan también por mail además de verse en la app? seguramente si
10.10. ¿Se permite reenviar documentos desde la app? seguramente si
10.11. ¿Se deben guardar versiones históricas de documentos o solo la última versión? si el usuario no guardo alguno de esos, seguramente no se permita ver nada anterior, osea la app solo dejaria ver, descargar, reenviar y demas solo lo mas actualizado en cada momento de los documentos que apliquen.
10.12. ¿La app debe mostrar número de factura, número de recibo, fecha y detalles de pago?  esta medio en la info enviada, pero como veas acorde
10.13. ¿La compra puede tener varias transacciones asociadas o solo una final?  no se bien esto, pero como veas acorde
10.14. ¿Qué pasa con una compra que quedó impaga pero no llegó a derivarse a justicia todavía? eso de derivar a la justicia es medio teorico, no le des tanta importancia, lo principal seria bloquear la cuenta si es que no cumple el plazo de pago de una multa
10.15. ¿El usuario puede filtrar compras por estado, subasta, fecha o tipo de documento?  Por subasta y tipo de documento seguramente no, igual esta medio en la info enviada, pero como veas acorde

11. Consignación de bienes
11.1. ¿Quién puede consignar: cualquier usuario aprobado o solo quienes tengan cierta categoría? cualquiera, sin importar categoria
11.2. ¿Se requiere sí o sí tener un medio de pago verificado para consignar? seguramente si, para despues poder pagar el envio de devolucion de un articulo ya enviado a la empresa, si hace falta. AUNQUE BUENO, IMPORTANTE, QUIZAS EL USUARIO PODRIA SIMPLEMENTE INGRESAR UN METODO DE PAGO REGISTRADO EN ESE PROCESO, Y QUE LA EMPRESA LO PUEDA VALIDAR EN ESE PROCESO GENERAL DE CONSIGNACION, CUANDO TAMBIEN DEBE VER SI LE SIRVE EL ARTICULO Y DEMAS, pero esto en general como veas acorde.
11.3. ¿Se requiere además una cuenta bancaria validada para recibir el pago de la venta? seguramente si, aunque podria ser la misma usada tanto para recibir como para pagar, ya que se podria guardar como lo mismo, una cuenta bancaria, y esa se podria usar para recibir el pago, y para pagar ese envio de devolucion si hace falta.
11.4. ¿El flujo de consignación comienza con aceptación de TyC y declaración jurada? si
11.5. ¿Qué campos son obligatorios al cargar el bien?  esta en la info enviada, en general todos con al menos algo de info.
11.6. ¿Cuántas fotos mínimas y máximas se piden? minimo 6, maximo puede ser tipo 15 o 20
11.7. ¿Qué formatos de foto se aceptan? no se bien, como veas acorde, pero nada raro, supongo que jpg, png, con la camara en ese momento, etc.
11.8. ¿Se permiten varios tipos de documentos de origen lícito? seguramente si
11.9. ¿Cuándo se solicita documentación de origen: al inicio, si hay duda o ambas? el usuario lo puede dar al inicio sin necesidad de que la empresa lo pida, pero puede no hacerlo, y si en el proceso de validacion inicial, la empresa duda, entonces puede pedirle al usuario que envie esos documentos para comprobacion de origen licito.
11.10. ¿La empresa puede pedir documentación adicional luego de una revisión inicial? si, eso de origen licito
11.11. ¿Qué estados puede tener una consignación?  esta medio en la info enviada, pero como veas acorde
11.12. ¿Qué significa exactamente cada estado del flujo de consignación?  esta medio en la info enviada, pero como veas acorde
11.13. ¿El usuario puede seguir el timeline completo de su bien dentro de la app? si
11.14. ¿La empresa puede rechazar una consignación sin inspección física? si
11.15. ¿Qué pasa si el usuario no acepta el valor base o la comisión propuesta? la empresa no negocia nada de eso, entonces se cancela todo, y el usuario deberá pagar por envio de devolucion o ir a retirarlo, ya que en ese punto, ya habria enviado el articulo a la empresa.
11.16. ¿Qué pasa si el usuario no retira ni paga la devolución del bien rechazado? Si ya fue enviado, rechazado/cacelado por alguna razon ahi, y no se concreta la devolucion, entonces la empresa hace lo que quiera con eso, por ej, donarlo
11.17. ¿El usuario puede volver a consignar el mismo bien rechazado más adelante?  no sabria bien como comprobar eso, pero como veas acorde
11.18. ¿Existe un bloqueo para evitar reenvío duplicado del mismo artículo? no se bien como comprobar eso, pero como veas acorde
11.19. ¿Qué pasa si la empresa acepta el bien pero luego decide unirlo a una colección? eso de coleccion en general no implica nada, seria seguramente una forma de llamar a una subasta, que en general, serian en torno a alguna tematica o cosas por el estilo, pero no le des tanta importancia, osea no creo que sea una estructura o caracteristica especifica, sino por ej, algo dentro del nombre posible para una subasta (Por ej., Colección Vanguardia). Si te referis a que la empresa decida crear una subasta medio tematica en torno a todos articulos de un usuario, entonces eso no cambia mucho el funcionamiento general, lo hace todo la empresa manual, y seria simplemente ponerle el nombre que quiera, (Por ej., Colección Pablo Martínez), y que los articulos que agreguen sean los de ese usuario, pero no seria nada diferencial en terminos de funcionamiento, estructuras internas ni nada, seria simplemente una decision de la empresa hacerlo o no, pero funcionaria como cualquier otra subasta normal.
11.20. ¿Cuándo se define si una subasta será individual o una colección del usuario? lo hace la empresa cuando quiera, en general, seguramente seria que la empresa primero cree las subastas con los datos acordes que quiera, y despues ir agregando los articulos que quiera (de los subidos por los usuarios y con todo validado, aprobado, acordado y demas) a la subasta que quiera.
11.21. ¿Hay una cantidad mínima de bienes para considerar una subasta tipo colección? otra vez, eso de coleccion no es una caracteristica, ni booleano ni nada, como ya dije, no le des mucha importancia, porque podria ser simplemente algo dentro del nombre que la empresa le quiera dar a alguna subasta particular.
11.22. ¿Qué ocurre si nadie puja por un artículo consignado? si fue subido, aprobado y todo, se lo agrego a una subasta, y esa subasta paso y nadie pujo por ese articulo, entonces la empresa compra ese articulo al precio base.
11.23. ¿La empresa compra el artículo al precio base al cierre? si nadie pujo por el, entonces si.
11.24. ¿Cómo se calcula y muestra la liquidación al vendedor?  esta medio en la info enviada, pero como veas acorde
11.25. ¿La liquidación se envía por mail y también queda visible dentro del detalle del bien? seguramente si, ambos
11.26. ¿El dueño puede ver la ubicación física del bien y la póliza de seguro? los detalles de poliza definitivamente si, y la ubicacion fisica del bien puede ser,  esta medio en la info enviada, pero como veas acorde
11.27. ¿El aumento de póliza por parte del usuario está fuera de la app o debe ofrecerse algún dato informativo? es todo fuera de la app eso del aumento de poliza, asi que no importa nada de eso, lo unico si podria ser potencialmente en la UI, agregar algun mensaje tipo “Contactate con la aseguradora si queres aumentar el valor de poliza” o algo de ese estilo, pero seria simplemente ese texto informativo y literal nada mas que eso, dentro de la app, la empresa de subastas en si y demas, no se hacen cargo de nada del proceso de aumento de poliza.

12. Notificaciones, alertas y mensajes
12.1. ¿La app tendrá notificaciones push o solo notificaciones internas dentro de la app? al menos por ahora, solo notificaciones internas, pero potencialmente, si es medio sencillo de hacer, se podrian hacer notificaciones push tambien, pero eso en un futuro, si hay tiempo.
12.2. ¿Qué eventos deben generar notificación obligatoriamente?  esta en general en la info enviada
12.3. ¿Qué categorías de notificación existirán exactamente?  esta medio en la info enviada, pero como veas acorde
12.4. ¿Las notificaciones se pueden filtrar por tipo, leídas/no leídas o fecha? por fecha seguramente no, igual esta medio en la info enviada, pero como veas acorde
12.5. ¿Debe existir contador global de no leídas? no se, como veas acorde
12.6. ¿El usuario puede marcar una notificación como leída individualmente y también todas a la vez? seguramente si
12.7. ¿Las notificaciones incluyen mensajes de subastas, pagos, consignaciones y estado de cuenta? seguramente si
12.8. ¿Los documentos generados también aparecen como notificaciones? no creo
12.9. ¿Se debe notificar por mail y dentro de la app en paralelo? no, osea solo algunas cosas llegarian por mail, no todo, pero si que la mayoria de cosas que lleguen por mail, serian tambien notificacion de la app.
12.10. ¿Las alertas de puja superada deben ser instantáneas? quizas no hacer notifiaciones/alertas de puja, tipo si fue superada o algo, porque al tener una puja ganadora, tenemos esa retencion de 1 min en esa pantalla, hasta que gane esa puja o sea superada, entonces basicamente, esas notificaciones/alertas por puja serian innecesarias/redundantes, ya que se enteraria de todo eso en la misma pantalla, osea lo estaria viendo en vivo.
12.11. ¿Qué mensajes se muestran en casos de cuenta restringida o bloqueada? si es restringida, entonces seguramente mostrar lo mismo que de normal, pero si es bloqueda, entonces ni siquiera podra acceder al apartado de notificaciones, y en caso de despues aplicar notificaciones push, no le deberian llegar nada de eso.
12.12. ¿Qué mensajes se muestran en caso de error de red o fallo temporal?  esta medio en la info enviada, pero como veas acorde
12.13. ¿Se muestran popups, banners, modales o pantallas completas según el tipo de evento?  esta medio en la info enviada, pero como veas acorde
- ADICIONALMENTE, UNA CONSIDERACIÓN IMPORTANTE SOBRE LAS NOTIFICACIONES: seguramente se podrian almacenar en la BD todas las notificaciones generadas, y una vez son marcadas como leidas por el usuario, activar un timer para que se eliminen despues de x tiempo que haya quedado marcada como leida. Y ademas, posiblemente definir un tiempo global de eliminacion, que aunque no se marque como leida cada notificacion, si pasa x tiempo desde su generacion, entonces se eliminara sin importar ninguna otroa condicion. Todo esto como para no sobrecargar la BD.

13. Historial y métricas
13.1. ¿Qué define exactamente el historial del usuario?  esta medio en la info enviada, pero como veas acorde
13.2. ¿El historial incluye solo pujas ganadas o también pujas perdidas y participaciones? seguramente sea para pujas superadas, pujas ganadas (articulos adquiridos), y pujas perdidas (osea no de adquirio el bien). 
13.3. ¿Qué métricas deben mostrarse sí o sí en la primera versión?  esta medio en la info enviada, pero como veas acorde
13.4. ¿Las métricas son por mes, trimestre, año o por período libre? seguramente esos filtros, mes, trimestre, año, total, pero como veas acorde
13.5. ¿Qué gráficos o visualizaciones se esperan?  esta medio en la info enviada, pero como veas acorde
13.6. ¿Se mostrarán totales de pujados, pagados, ganados, perdidos y consignados?  esta medio en la info enviada, pero como veas acorde. EN GENERAL, IMPORTANTE, podrian ser 2 tabs/apartados principales, 1 para la informacion referente al proceso de pujas/subastas, y otro para la informacion referente al proceso de consignacion/ventas.
13.7. ¿Se mostrará porcentaje de éxito en subastas? puede ser si
13.8. ¿Se mostrará categoría favorita o segmento más usado? puede ser si
13.9. ¿El usuario verá un ranking o solo estadísticas propias? seguramente solo estadisticas propias, pero como veas acorde, puede ser un agregado
13.10. ¿El historial se separa entre participación como postor y actividad como vendedor? podria ser si, osea para estadisticas seguramente si hacer eso, osea estadisticas como postor y otro tab para estadisticas como vendedor, y tambien, si lo ves acorde, se podria hacer lo mismo para el historial de actividad, aunque quizas para un historial de actividad como vendedor, eso ya medio que se veria en el apartado de consignacion, donde no esta explicito eso del historial como vendedor, pero si que se podrian ver todos los articulos subidos, pudiendo filtrar por activo, vendido, rechazado, y demas detalles, entonces quizas no haria falta algo de historial de actividad como vendedor; pero por otra parte, si que podria servir algo de estadisticas mas globales como vendedor/consignacion.

14. Estados auxiliares, vacíos y errores
14.1. ¿Qué estados vacíos se quieren contemplar desde el diseño?  esta medio en la info enviada, pero como veas acorde
14.2. ¿Qué debe mostrarse cuando no hay subastas disponibles? seguramente alguna pantalla indicando eso, osea la misma pantalla, con todo lo mismo en general, pero que tenga algun estado/posibilidad que sea con algun mensaje informativo apropiado y demas, reemplazando las cards que deberia haber de subastas, pero como veas acorde
14.3. ¿Qué debe mostrarse cuando no hay medios de pago cargados? Lo mismo que cuando estan cargados, pero no habria ninguna card de metodo de pago, y posiblemente algun mensaje informativo acorde en ese espacio donde estarian esas cards de los metodos de pago guardados
14.4. ¿Qué debe mostrarse cuando no hay compras, notificaciones o consignaciones? En general lo mismo como todo lo ya mencionado, la misma pantalla, con todo lo mismo, pero con algun mensaje informativo acorde, y quizas algun icono que acompañe o algo del estilo, en general como veas acorde.
14.5. ¿Qué debe mostrarse cuando el usuario no tiene permisos para una acción? esta medio en la info enviada, pero como veas acorde, y segun cada situacion, puede ser popup, si es invitado seria seguramente esa pantalla de funcion limitada por invitado, etc.
14.6. ¿Qué estados de error deben resolverse con pantalla completa y cuáles con popup?  esta medio en la info enviada, pero como veas acorde
14.7. ¿Qué pasa si falla una carga de imagen o documento? seguramente algun mensaje acorde que lo reemplace, indicando eso que paso, osea como algun estado generico de fallback, pero en general como veas acorde.
14.8. ¿Qué pasa si se pierde conexión durante una puja o un pago? no se bien, como veas acorde
14.9. ¿Qué pasa si un endpoint responde tarde o no responde? no se bien, como veas acorde
14.10. ¿Se necesita reintento manual o automático en algunos casos? no se bien, como veas acorde
14.11. ¿Qué mensajes deben ser visibles para el usuario y cuáles solo para soporte o logs? no se bien, como veas acorde

15. Backend, integración y datos
15.1. ¿Qué parte del sistema existente se consume tal cual y qué parte se crea nueva? en general, de endpoints y demas para acceso desde frontend, no hay nada del sistema existente, osea lo unico que se sabe del sistema existente es la estructura SQL ya enviada, pero sacando eso, seria todo nuevo de la app propia. Como ya fue mencionado, lo unico a acoplar con el sistema existente seria esa estructura SQL, pero eso se veria involucrado unicamente en backend, donde tambien se deberian agregar todas las tablas nuevas necesarias para todo lo de la app nueva en si.
15.2. ¿Qué tablas actuales se usarán directamente y cuáles requieren tablas nuevas de apoyo? esto como veas acorde, en general, todo lo que se pueda usar de lo ya existente, usalo, y para todo lo que haga falta crear nuevas tablas de apoyo, entonces hacelo, no hay problema por eso, pero recorda que para las tablas ya existentes, no se puede modificar ni agregar atributos, relaciones ni nada, osea no se puede cambiar ni agregar nada a las declaraciones de las tablas de la estructura actual SQL del sistema ya existente.
15.3. ¿Qué nuevas entidades habrá que crear para la app? todo lo que veas acorde
15.4. ¿Cómo se mapearán los usuarios de la app con personas, clientes y duenios? esto como veas acorde, pero en general que sea compatible con ese sistema ya existente, intentando no generar redundancia donde no haga falta.
15.5. ¿Cómo se manejará la autenticación de los usuarios con la estructura actual de datos? no se, como veas acorde
15.6. ¿La app necesita persistir notificaciones, documentos, sesiones y estados temporales en tablas nuevas? seguramente si, pero todo como veas acorde
15.7. ¿Qué campos de la base actual deben considerarse de solo lectura? en general como veas acorde, pero creo que no habria algun campo o tabla especifico que no se pueda escribir
15.8. ¿Qué validaciones deben quedar en backend sí o sí aunque el frontend también las haga? no se, como veas acorde
15.9. ¿Qué acciones necesitan transacciones para no dejar datos inconsistentes? no se, como veas acorde
15.10. ¿Qué procesos requieren control de concurrencia fuerte, como pujas y pagos? no se, como veas acorde
15.11. ¿Qué datos deben registrarse como auditoría completa? no se, como veas acorde
15.12. ¿Qué errores deben trazarse para soporte técnico? no se, como veas acorde, aunque pensa que tampoco habria algo de soporte de verdad, osea el soporte de la app y demas, seria algo mas teorico, pero hacelo como veas acorde.

16. Reglas de negocio críticas
16.1. ¿Una puja siempre debe superar a la anterior por un mínimo basado en el precio base? Si es subasta oro o platino, entonces seria simplemente superarlo en 1 unidad, sea ARS o USD como minimo, pero si es de otra categoria la subasta, entonces seria ese minimo del mejor valor de puja anterior, sumado a ese 1% del valor de precio base del articulo, eso como minimo de la siguiente puja.
16.2. ¿Ese mínimo cambia por categoría de subasta? en general no, osea es o aplica el limite o no, pero no varia ese 1%, la unica variacion es que no aplica el minimo para subastas oro o platino.
16.3. ¿El máximo de puja también depende de la categoría? Funciona igual que para el minimo, el maximo es el mejor valor de puja hasta el momento, mas el 20% del valor de precio base del articulo. Pero al igual que para el minimo, no aplica ese maximo para subastas oro o platino, seria sin limite inferior ni superior para esas subastas, solo que supere al menos por una unidad de la moneda, a la mejor puja anterior obviamente, porque no puede ser exactamente igual o inferior al mejor valor de puja ya ofertado. Y bueno, cuando todavia nadie pujo, sin importar la subasta ni su categoria, entonces se podra pujar por el valor de precio base de ese articulo, es decir, para la 1ra puja, si se quiere, puede ofertarse por ese precio base.
16.4. ¿Qué pasa con subastas oro y platino respecto de los límites? no se aplican esos de 1% minimo ni 20% maximo, como ya fue mencionado.
16.5. ¿Un usuario puede pujar en varias subastas durante el mismo día si solo tiene una activa a la vez? si
16.6. ¿La moneda de la subasta define todo el flujo económico de esa subasta? si osea, si una subasta esta en pesos argentinos por ej, entonces todas las pujas y todos los articulos estaran con valores en pesos argentinos.
16.7. ¿Puede haber una subasta en pesos pero con medios de pago en USD o viceversa? no, no se podran usar metodos de pago de una moneda, en subastas que tengan otra moneda.
16.8. ¿La categoría del usuario se usa solo para habilitar participación o también para definir topes económicos? en terminos de reglas definidas, seria solo para habilitar participacion, y bueno, eso ya definido de no aplicar los limites de 1% ni 20% en las subastas oro o platino.
16.9. ¿La empresa puede pausar una subasta o un lote en vivo? no creo, siento que seria una funcion quizas dificil de implementar y medio innecesaria, por lo que seguramente no haria nada de eso.
16.10. ¿La empresa puede cancelar una subasta ya publicada? nuevamente, eso seria seguramente una complciacion innecesaria, quizas poder editar algo de la info general, pero cancelarla no creo, y bueno, esas posibilidades seria por fuera de la app en si, sino unicamente por ej, con endpoints auxiliares de backend, o modificar cosas directamente con la interfaz de interfaz de postgresql y hacer queries sql ahi.
16.11. ¿Qué pasa con las pujas de una subasta cancelada? seguramente nada de subasta cancelada
16.12. ¿Qué pasa con compras o consignaciones si cambia el estado general del sistema? no se bien a que te referis con esto, pero seguramente nada de eso, igual como veas acorde.

17. UX, navegación y pantallas
17.1. ¿Qué pantallas están confirmadas como principales y cuáles son auxiliares? esta medio en la info enviada, pero como veas acorde
17.2. ¿La app tendrá bottom nav, menú lateral o ambos? ambos, bottom nav y tambien en ese bottom nav, esa posibilidad de desplegar ese menu con algunas otras opciones/acceso a otras pantallas/apartados
17.3. ¿Qué cinco accesos principales deberían quedar siempre visibles? como ya fue mencionado, en ese bottom nav, estarian Notificaciones (NOTIF.) - CONSIGNAR- SUBASTAS (este seria el central) - COMPRAS - MENÚ
17.4. ¿Qué pantallas pueden abrirse como modal en vez de pantalla completa? esta medio en la info enviada, pero como veas acorde
17.5. ¿Qué pantallas requieren navegación con retorno al estado previo? esta medio en la info enviada, pero como veas acorde
17.6. ¿Qué pantallas necesitan mantener filtros, tabs o scroll al volver? esta medio en la info enviada, pero como veas acorde
17.7. ¿La app debe tener un estilo visual muy cercano al diseño original o se permite simplificar pantallas? en general, deberia quedar lo mas parecido posible al diseño original hecho en figma; de forma general, cambiando/agregando solo lo de verdad necesario.
17.8. ¿Qué pantallas pueden fusionarse sin perder claridad funcional? esta medio en la info enviada, pero como veas acorde, y nuevamente, solo cambiando/agregando lo de verdad necesario
17.9. ¿Qué acciones deben estar siempre a un toque de distancia? esta medio en la info enviada, pero como veas acorde
17.10. ¿Qué cosas solo se muestran cuando el usuario entra al detalle? esta medio en la info enviada, pero como veas acorde

18. Decisiones técnicas de implementación
18.1. ¿Se usará Axios o fetch nativo? lo que te parezca mejor, en general intentando algo simple, que no sea demasiado complejo, pero tambien buscando que sea solido y funcione bien.
18.2. ¿Se usará React Query para cache, reintentos y sincronización de datos? no se, como veas acorde
18.3. ¿Qué parte del estado manejará Zustand y qué parte React Query? no se, como veas acorde
18.4. ¿Las notificaciones en tiempo real irán por WebSocket, polling o combinación? entiendo que para eso de subastas, pujas y demas, en general seria por websockets, o lo que sea mejor/mas solido en ese contexto
18.5. ¿El streaming externo se abre dentro de la app, en webview o fuera de la app? Pensa que eso de streaming en vivo, de poder ver la subasta en video y demas, no seria nada de mi app, no hace falta implementar ni adaptar nada para eso.
18.6. ¿La app necesita manejo offline parcial o solo pantalla de error? esta medio en la info enviada, pero como veas acorde
18.7. ¿Se guardará sesión, filtros o borradores localmente? no se, como veas acorde
18.8. ¿Se subirán imágenes y documentos como multipart, Base64 o ambos? esta medio en la info enviada, pero como veas acorde
18.9. ¿Qué tamaño máximo de archivos se aceptará? no se, como veas acorde
18.10. ¿Qué validaciones se harán en frontend y cuáles exclusivamente en backend? no se, como veas acorde
18.11. ¿Se necesitan interceptores globales para tokens y errores? no se, como veas acorde
18.12. ¿Se manejarán roles y rutas protegidas desde el frontend? no se, como veas acorde
18.13. ¿Cómo se sincronizarán datos del backend con actualizaciones en tiempo real? no se, como veas acorde
18.14. ¿Se usará una estrategia de cache por pantalla o por entidad? no se, como veas acorde
18.15. ¿Qué endpoints requieren refresco automático de datos? no se, como veas acorde

19. Administración y endpoints auxiliares
19.1. ¿Qué endpoints de administración se van a crear solo para pruebas internas? todos los endpoints de administracion serian para pruebas internas, porque en realidad, para la app, la entrega y demas, no es necesario nada de eso, pero se hace para hacer mas faciles las pruebas. En ese contexto, hace todos los endpoints y demas de ese modo admin segun veas acorde y necesario/util.
19.2. ¿Qué acciones administrativas serán útiles para validar el sistema? esta medio en la info enviada, pero como veas acorde. Osea cosas como validar usuarios, asignarles categoria, validar metodos de pago, validar articulos consignados, proponer un acuerdo de consignacion, crear subastas, agregar articulos a subastas, entre otros, pero en general, lo que te parezca acorde y que sirva. Y bueno, ademas de eso, pensa que seguramente haria falta eso del sistema de excepciones, segun cierta probabilidad (sucesos aleatorios), para generar esas situaciones posibles en que pasa algo con el metodo de pago (por ej. tarjeta bloqueada), no se puede pagar el articulo al ganar una puja, y entonces se genera la multa.
19.3. ¿Se necesita un modo admin separado en el backend aunque no tenga UI? en general, me parece que no, osea simplemente con tener esos endpoints seguramente ya iria bien creo
19.4. ¿Qué estados de usuarios, medios de pago, subastas, consignaciones y documentos conviene poder forzar desde admin? esta medio en la info enviada, pero no se bien, eso como veas acorde
19.5. ¿Qué datos conviene poder cargar de prueba para demo o testing? no se, como veas acorde
19.6. ¿Habrá endpoints para simular aprobaciones, rechazos, bloqueos o pujas ganadoras? esta medio en la info enviada, pero como veas acorde
19.7. ¿Habrá endpoints para generar datos semilla o resetear escenarios? no se, como veas acorde, pero puede ser si te parece que es algo simple de hacer y ayudaria/seria util.
19.8. ¿Se necesitan endpoints para reemitir mails o volver a generar documentos? puede ser, para generar documentos quizas no, pero como veas acorde

20. Definición final de entregable
20.1. ¿La primera entrega debe cubrir solo el flujo principal o también todos los estados auxiliares? en general, intentar cubrir todo bien, todo completo y sin dejarse nada relevante.
20.2. ¿Qué tan fiel debe ser la app al diseño original en pantallas y navegación? en general, lo maximo posible, pero si hace falta cambiar o agregar algo, se puede hacer, tampoco es que no se pueda cambiar nada, pero en general, solo agregar/cambiar cosas cuando de verdad haga falta o mejore de verdad algo de lo diseñado/planificado inicialmente (basicamente, no cambiar/agregar algo porque si, sin razon valida)
20.3. ¿Qué cosas se pueden simplificar sin romper la idea general? no se bien eso, diria de en general no buscar cambiar mucho, pero como veas acorde
20.4. ¿Qué cosas no se pueden tocar porque forman parte de la lógica central?  esta medio en la info enviada, pero como veas acorde. Aunque en general no cambiar nada que no haga falta.
20.5. ¿Qué funcionalidades deberían quedar sí o sí en el MVP? en general todo lo ya mencionado y enviado en la info, que todo quede implementado y funcionando correctamente.

