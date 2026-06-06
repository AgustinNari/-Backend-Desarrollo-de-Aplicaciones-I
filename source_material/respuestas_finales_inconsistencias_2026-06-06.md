# Respuestas finales a inconsistencias — 2026-06-06

Este archivo conserva el banco de preguntas y respuestas usado para resolver contradicciones. La fuente final resumida es `docs/00_decisiones_finales.md`, `docs/API_CONTRATO_FINAL.md` y `docs/10_plan_correcciones_backend.md`.

---

Banco de preguntas Sí/No para cerrar inconsistencias QuickBid
A) Contrato general y documentación
Contexto: hay varias diferencias chicas entre documentos, .md internos y código. Conviene definir si el backend debe adaptarse a los documentos originales o si se documenta lo que ya implementó el backend.
¿Querés que el backend se adapte lo máximo posible al contrato original de endpoints que habías definido?En general si, pero si son cambios minimos o cosas necesarias, no habria un gran problema
¿Querés crear/dejar un archivo único tipo docs/API_CONTRATO_FINAL.md como fuente final de endpoints, DTOs, enums y reglas?Si
¿Querés que, cuando haya contradicción entre documentos viejos y backend actual, se priorice el contrato original salvo que haya una razón técnica fuerte para mantener el backend actual?no
¿Querés que las diferencias inevitables del esquema legacy adaptado a PostgreSQL queden documentadas explícitamente como normalizaciones técnicas?si
¿Querés que los endpoints admin sigan siendo auxiliares solo para pruebas y gestión manual, sin frontend propio?si

B) Registro, login, tokens y cuenta bloqueada
Contexto: el flujo de registro está bastante bien, pero hay dudas con duración de tokens, cuenta bloqueada y cambio de contraseña desde sesión activa.
¿Querés que una cuenta bloqueada_permanente pueda iniciar sesión de forma limitada para ver solo una pantalla informativa de bloqueo?si
¿Querés que una cuenta bloqueada_permanente NO pueda navegar ninguna función real de la app aunque el login limitado sea exitoso?exacto
¿Querés que el backend devuelva en el login el estado de cuenta para que el frontend sepa si debe mostrar pantalla de bloqueo?si
¿Querés que el access token dure 15 minutos?si
¿Querés que el refresh token dure 30 días?si
¿Querés que el token de recuperación de contraseña dure 30 minutos?si
¿Querés que el token final de registro/setup de contraseña dure 48 horas?si
¿Querés que los tokens de setup/recuperación sean de un solo uso?si
¿Querés permitir cambio de contraseña desde sesión activa sin usar token por mail?si. Es decir, tener las 2 opciones, tanto recuperacion desde login, como tambien cambio de contraseña desde sesion activa.
¿Querés mantener recuperación de contraseña por mail para usuarios que olvidaron la clave?si
¿Querés que el campo del request de etapa 3 se llame setup_token en vez de token?si
¿Querés que el backend invalide el link anterior cuando se reenvía un nuevo link de finalización de registro?si
¿Querés que el backend mantenga mensajes genéricos para recuperación/reenvío y así no revelar si un email existe o no?exacto

C) Archivos de registro y DNI
Contexto: una parte de pantallas mencionaba PDF para DNI, pero las reglas más consistentes y el backend apuntan a imágenes.
¿Querés que el DNI acepte únicamente imágenes y no PDF?exacto
¿Querés que los formatos aceptados para DNI sean JPG/JPEG, PNG y WebP?si ahora esta solo con jpg/jpeg y png, esta bien, no haria falta cambiarlo.
¿Querés que frente y dorso del DNI sean obligatorios como dos archivos separados?si
¿Querés mantener un límite configurable de tamaño para imágenes de DNI?si

D) Endpoints y nombres de parámetros
Contexto: hay diferencias menores que pueden romper el frontend si no se unifican.
¿Querés que POST /api/auth/registro/etapa2 use los nombres fotoFrenteDni y fotoDorsoDni?si
¿Querés que POST /api/subastas/{id}/verificacion quede sin tilde definitivamente?si
¿Querés que marcar todas las notificaciones como leídas sea con endpoint separado /api/usuario/notificaciones/all/leer?no, si se puede y no cambia mucho, seguramente mejor hacerlo como esta en la documentacion envidada de los endpoints para este caso.
¿Querés evitar usar {id}=all en el endpoint individual de marcar notificación como leída?seguramente no, sino quizas usar ese all en lugar del endpoint aparte; pero esto unicamente si de verdad no cambia mucho el funcionamiento y no lo hace peor, porque si afecta negativamente de verdad, entonces hacer endpoint aparte y listo.
¿Querés que GET /api/usuario/estadisticas acepte y aplique realmente el filtro periodo? si
¿Querés que GET /api/compras acepte y aplique realmente filtro por estado? si
¿Querés que los endpoints públicos de subastas/catálogo/items sigan funcionando sin token para modo invitado?si eso no impide las reglas necesarias para vista de invitado, entonces si.

E) Invitado y datos públicos
Contexto: está bastante claro que el invitado puede ver subastas/catálogos/items, pero sin precios ni información viva. Igual conviene cerrar detalles.
¿Querés que el invitado nunca vea precio base?exacto
¿Querés que el invitado nunca vea mejor oferta actual?exacto
¿Querés que el invitado nunca vea historial de pujas?exacto
¿Querés que el invitado nunca vea cantidad de pujas?exacto
¿Querés que el invitado nunca vea qué ítem está actualmente en vivo?exacto
¿Querés que el invitado sí pueda ver descripción, imágenes y datos generales de cada ítem?si
¿Querés que el invitado pueda ver si una subasta está activa o futura, pero sin detalles del progreso interno?si
¿Querés que cualquier intento de pujar/consignar/comprar como invitado devuelva bloqueo por falta de login?puede ser

F) Inscripción a subastas
Contexto: documentos dicen 30 minutos antes; backend actual parece usar 60. También hay duda sobre reinscripción luego de rechazo.
¿Querés que la inscripción a subasta pueda hacerse hasta 30 minutos antes del inicio?no
¿Querés descartar la regla actual de 60 minutos?no
¿Querés permitir inscripción con medio de pago pendiente de verificación?si
¿Querés permitir inscripción con medio de pago vencido para que la empresa pueda revalidarlo?si
¿Querés que una inscripción rechazada no bloquee un nuevo intento de inscripción en la misma subasta?exacto, no podra usar un metodo de pago rechazado para esa inscripcion, pero si algun otro que tenga en otro estado.
¿Querés que el usuario pueda reinscribirse con otro medio si la inscripción anterior fue rechazada?si
¿Querés conservar la inscripción rechazada como historial aunque se permita una nueva?puede ser
¿Querés mantener que la inscripción NO crea asistencia?exacto
¿Querés mantener que la asistencia se cree recién con la primera puja aceptada?exacto
¿Querés que un medio validado por inscripción pueda usarse en cualquier subasta de la misma moneda mientras siga vigente?exacto, sea validado por inscripcion o por el proceso inicial de validación del metodo despues de agregarlo inicialmente; mientras el metodo de pago tenga validacion vigente, entonces sera valido para subastas de la misma moneda
¿Querés que la inscripción sirva también para enviar notificaciones/email de inicio de subasta si fue aprobada?exacto

G) Medios de pago
Contexto: la estructura general está bien, pero hay dudas sobre revalidación, límites, vencimiento y días hábiles.
¿Querés que la verificación de un medio de pago dure 5 días hábiles?si
¿Querés que al vencer la verificación el medio pase automáticamente a estado vencido?si
¿Querés agregar o mantener un job automático para marcar medios expirados como vencido?puede ser
¿Querés que un admin pueda revalidar un medio en estado vencido sin que el usuario tenga que cargarlo de nuevo?si
¿Querés que un admin pueda revalidar un medio ya verificado para extender su vigencia?si, pero nunca pasar de esos 5 dias habiles, osea poder revalidarlo, pero simplemente seria para reiniciar el timer de 5 dias habiles.
¿Querés que al validar un medio la empresa deba cargar obligatoriamente un limiteAprobado?si, ese limite siempre debe ser responsabilidad de la empresa(empleado) cargarlo.
¿Querés evitar que un medio verificado sin límite cargado se interprete como “sin límite/infinito”?exacto
¿Querés que tarjetas y cuentas bancarias tengan consumo acumulado con reinicio temporal?seguramente no, porque seria hacer las cosas demasiado complejas. Seguramente dejar esa revalidacion/ajuste de limites al momento en que la empresa valida/revalida un metodo de pago y listo.
¿Querés que tarjetas tengan reinicio mensual de consumo?seguramente no, por lo explicado recien arriba
¿Querés que cuentas bancarias tengan consumo rolling de 30 días?seguramente no, por lo explicado recien arriba
¿Querés que cheques certificados consuman contra un monto fijo sin reinicio mensual?exacto
¿Querés mantener un único medio principal por moneda?exacto
¿Querés permitir como máximo dos principales: uno ARS y uno USD?exacto
¿Querés mantener que los medios de pago no se puedan editar, salvo marcar principal?exacto
¿Querés que eliminar un medio sea siempre borrado lógico?exacto
¿Querés impedir eliminar medios asociados a operaciones pendientes?exacto
¿Querés que la validación de medio pueda sumar puntos solo cuando la empresa lo aprueba?exacto
¿Querés que un medio recién registrado nunca pueda usarse hasta ser validado?exacto, a menos que sea para acciones puntuales que sí permitan usar medios de pago en proceso de validación/recien registrados, como puede ser la inscripción a una subasta.

H) Puntos, reputación y categoría
Contexto: los valores documentados y los valores del código no coinciden.
¿Querés usar como verdad final los valores de puntos documentados originalmente?si, usar eso y listo
¿Querés que validar un medio de pago sume +30 puntos?
¿Querés que cada puja aceptada sume +1 punto?
¿Querés que ganar una puja sume +80 puntos?
¿Querés que completar el pago de compra a tiempo sume +60 puntos?
¿Querés que una consignación aceptada y finalmente puesta en subasta sume +70 puntos?
¿Querés que pagar comisiones/envío dentro del plazo sume +20 puntos?
¿Querés que generar una multa reste -90 puntos?
¿Querés que una multa vencida sin pago reste -250 puntos?
¿Querés que los puntos nunca bajen de 0?exacto
¿Querés que la categoría pueda subir y bajar automáticamente según puntos?exacto
¿Querés conservar estos rangos: común 0-249, especial 250-699, plata 700-1499, oro 1500-2999, platino 3000+?si
¿Querés que la empresa pueda ajustar manualmente puntos desde endpoint admin?si
¿Querés que el ajuste manual de puntos recalcule automáticamente la categoría?si
¿Querés guardar historial de movimientos de puntos aunque el usuario solo vea el total/categoría actual?si

I) Pujas y tiempo real
Contexto: las reglas base están claras, pero falta decidir cierre automático, reserva de límite y notificaciones persistentes.
¿Querés implementar cierre automático del lote luego de 60 segundos sin que superen la mejor puja?exacto
¿Querés que ese cierre automático avance automáticamente al siguiente ítem de la subasta?puede ser, o quizas establecer un timer como de espera/delay, de otros 60 segundos, para que el usuario que gano el ultimo item tenga tiempo de volver a la subasta en vivo y demas para pujar por el nuevo item. Es decir, una vez que se cierra un item, dar un delay de 60 segundos hasta que el proximo se active y se pueda pujar por ese.
¿Querés que si el último ítem se cierra, la subasta quede finalizada automáticamente?si, o quizas cierto delay, por ej 1 o 2 min, por si hace falta para que no haya problemas medio raros, por ej, que se expulse directo a todos de la subasta o comportamientos raros. Es decir, seguramente dejar 120 segundos de delay desde el cierre del ultimo item hasta que se cierra la subasta total en si.
¿Querés mantener idempotencyKey obligatorio en cada puja?puede ser, eso segun tu criterio
¿Querés que el frontend bloquee enviar otra puja hasta recibir confirmación del backend?puede ser, eso segun tu criterio
¿Querés que el backend también rechace una segunda puja duplicada o concurrente con misma idempotencyKey?puede ser, eso segun tu criterio
¿Querés que una puja aceptada reserve capacidad/límite del medio de pago inmediatamente? Mientras esa puja sea ganadora si, pero si esa puja es superada, se deberia restaurar el limite de manera acorde a lo que corresponda. Es decir, mientras tenga una puja ganadora, hasta que no se la superen, se resta eso del limite; si se la superan, se revierte ese impacto al limite, y si termina ganando esa puja, entonces esa alteracion del limite ya seria efectiva/real.
¿Querés que solo la puja ganadora final consuma realmente el límite del medio?puede ser, eso segun tu criterio
¿Querés que una puja superada libere la reserva del medio de pago?seguramente si
¿Querés que el usuario no pueda tener una puja ganadora activa en otra subasta al mismo tiempo?exacto
¿Querés que el usuario pueda ver en vivo una subasta aunque no pueda pujar por falta de medio/categoría?exacto
¿Querés que las alertas de puja superada sean solo por WebSocket y no notificaciones persistentes?puede ser, eso segun tu criterio
¿Querés evitar guardar notificaciones persistentes por cada puja superada?puede ser, eso segun tu criterio
¿Querés mostrar el número de postor ganador sin mostrar nombre/email del usuario?exacto
¿Querés mantener que la primera puja pueda ser igual al precio base?exacto
¿Querés mantener que en subastas oro/platino solo se exija superar por 1 unidad?exacto
¿Querés mantener que en subastas no oro/platino aplique mínimo +1% y máximo +20% del precio base?exacto
¿Querés mantener que un usuario no pueda pujar por un producto propio?exacto

J) Compras, multas, pagos y ventas legacy
Contexto: el backend parece crear registro de venta/adjudicación incluso si falla el cobro inmediato. También falta cerrar cuándo se generan documentos y cómo se calculan comisiones.
¿Querés que registroDeSubasta se cree apenas se adjudica el ítem, aunque el cobro inmediato falle?puede ser, eso segun tu criterio
¿Querés que registroDeSubasta represente adjudicación comercial y no necesariamente pago completo?puede ser, eso segun tu criterio
¿Querés que si falla el cobro inmediato se genere compra con estado multa_activa?exacto
¿Querés que el valor del artículo y la multa deban pagarse juntos en un único pago?si
¿Querés mantener multa automática del 10% del valor ofertado?si
¿Querés mantener plazo de 72 horas para regularizar artículo + multa?si
¿Querés que si la multa vence sin pago la cuenta pase a bloqueada_permanente?si
¿Querés que pagar artículo + multa quite la restricción por multa activa?si
¿Querés que comisiones y envío se paguen en un segundo paso separado?si
¿Querés que si no se pagan comisiones/envío en plazo, la empresa pueda quedarse con el dinero ya pagado y con el ítem?si
¿Querés que ese abandono por falta de pago de extras sea automático por job?puede ser, eso segun tu criterio
¿Querés que ese abandono por falta de pago de extras pueda manejarse manualmente por admin?no creo, si no hace falta no
¿Querés mantener retiro personal y envío a domicilio como únicas modalidades de entrega?exacto
¿Querés que una vez pagadas comisiones/envío ya no se pueda cambiar la modalidad de entrega?exacto
¿Querés que la dirección de envío quede congelada al pagar extras?si
¿Querés que la comisión del comprador se calcule sobre el precio final ofertado?si. Es decir, no calcular sobre el precio base, sino sobre el precio final ofertado; el de la puja ganadora.
¿Querés que la comisión del vendedor se calcule sobre el precio final ofertado?exacto
¿Querés que si compra la empresa por falta de pujas no se cobre comisión comprador?exacto
¿Querés que si nadie puja, la empresa compre siempre al precio base?exacto, si se pasa el tiempo sin ninguna puja, entonces lo compra la empresa al precio base.
¿Querés que la compra interna de empresa use un cliente técnico/sistema?seguramente si, eso segun tu criterio

K) Documentos y PDFs
Contexto: documentos piden PDFs reales; el código parece tener generación parcial/simulada.
¿Querés que el backend genere PDFs reales y no solo bytes simulados?puede ser, eso segun tu criterio
¿Querés que el comprobante/factura de compra se genere cuando la compra queda completamente pagada?exacto, con pago de extras y todo.
¿Querés que el recibo de multa se genere solo cuando la multa se paga?exacto
¿Querés que el acuerdo de consignación se genere al aceptar el acuerdo?puede ser, eso segun tu criterio
¿Querés que la liquidación de venta se genere cuando se paga/liquida al vendedor? si
¿Querés que el comprobante de envío de devolución se genere cuando se paga el envío de devolución? si
¿Querés que el usuario vea solo la última versión válida de cada documento? si
¿Querés conservar internamente versiones históricas de documentos aunque no se muestren al usuario? puede ser, aunque quizas no hace falta tanto detalle, eso segun tu criterio
¿Querés que los documentos también puedan enviarse por mail además de verse en la app? si
¿Querés agregar endpoint para reenviar documentos por mail? puede ser si, eso segun tu criterio, aunque posiblemente permitir algo para cada documento, de tipo descargar, reenviar al mail o similares.
¿Querés que los documentos se conserven sin borrado automático?puede ser, eso segun tu criterio

L) Consignación
Contexto: el flujo está bien, pero hay dudas con fotos, checkboxes, estados y diferencia segmento/categoría.
¿Querés que aceptar acuerdo de consignación requiera obligatoriamente leyoContrato=true?puede ser, eso segun tu criterio
¿Querés que aceptar acuerdo de consignación requiera obligatoriamente aceptaClausulasPlazos=true?puede ser, eso segun tu criterio
¿Querés que una consignación requiera mínimo 6 fotos?si
¿Querés que una consignación permita máximo 15 fotos?si
¿Querés rechazar la creación de consignación si supera el máximo de fotos?exacto
¿Querés que segmento signifique rubro/tema del bien, por ejemplo arte, joyas, vehículos o relojería?exacto
¿Querés que categoria quede reservada para común/especial/plata/oro/platino?exacto
¿Querés separar explícitamente segmento y categoriaSubasta en backend/docs?seguramente si
¿Querés que documentacion-origen deje la consignación en estado documentacion_recibida?puede ser, aunque quizas pasarlo nuevamente a pendiente_revision o algo del estilo, ya que requeriria nuevamente una revision por parte de la empresa; y ese proceso en si seria parte del proceso general de la revision digital por parte de la empresa.
¿Querés dejar de usar pendiente_revision como estado posterior a subir documentación adicional?Quizas dejarlo como esta, revisa segun eso que te dije arriba recien.
¿Querés conservar un estado explícito rechazo_revision_fisica antes de pasar a devolución?puede ser, eso segun tu criterio
¿Querés que si el usuario rechaza el acuerdo pase a devolucion_pendiente?seguramente si
¿Querés que si la empresa rechaza en revisión física pase a devolucion_pendiente?seguramente si
¿Querés que el producto legacy se cree recién cuando el usuario acepta el acuerdo?exacto
¿Querés impedir cualquier rechazo posterior una vez que el producto legacy ya fue creado por acuerdo aceptado?exacto
¿Querés que una solicitud de consignación sea siempre 1 solicitud = 1 bien?exacto
¿Querés que colección sea solo un nombre/título comercial de subasta y no una entidad especial?exacto
¿Querés que para publicar una consignación sea obligatorio tener duenio legacy creado?No para inicializar una consignacion; pero si se requeriria mas adelante en el proceso de consignacion, si es que todavia no se tenia creado; es decir, antes de poder proponer el acuerdo, para despues crear el producto legacy y demas (si acepta el acuerdo), antes de eso, haria falta tener creado el registro en duenio legacy.
¿Querés que para proponer acuerdo sea obligatorio que el consignador ya esté validado como duenio?si
¿Querés que el verificador del dueño pueda ser distinto del revisor del producto?exacto. Porque pensa que el verificador en duenio pudo haber sido cualquier empleado, incluso alguno hace mucho tiempo, que hizo esa verificacion antes de proponer acuerdo para algun otro producto que ese mismo usuario habia subido para consignar, y ahora el revisor de ese nuevo producto a consignar puede ser cualquier otro (pero esto es solo un ejemplo)

M) Direcciones de envío
Contexto: documentos largos recomiendan hasta 5 direcciones; backend parece más simple.
¿Querés permitir hasta 5 direcciones de envío activas por usuario?si
¿Querés permitir solo una dirección de envío principal?puede ser, eso segun tu criterio
¿Querés que el usuario pueda administrar direcciones desde perfil?si
¿Querés que el domicilio legal sea distinto de las direcciones de envío?que pueda ser diferente, si, es decir, que no necesariamente tiene que ser el mismo
¿Querés que la compra copie/congele la dirección elegida al momento de pagar envío/comisiones?si
¿Querés que, para MVP, alcance con una sola dirección de envío editable? Es probable/posible que haya que tratarlo de una manera similar a los metodos de pago, sin poder editar, y solo permitir eliminacion, pero que implique borrado logico, eso para poder mantener buena consistencia en la BD y backend en general. Es decir, al menos de forma general, tratarlo de cierta manera como para los metodos de pago y su gestion.

N) Notificaciones
Contexto: backend tiene notificaciones simples; documentos proponen archivado y limpieza.
¿Querés mantener notificaciones simples con solo leida/no_leida?puede ser, eso segun tu criterio
¿Querés agregar estado archivada? quizas no hace falta, eso segun tu criterio
¿Querés permitir archivar notificaciones desde la app? seguramente no
¿Querés eliminar automáticamente notificaciones leídas después de 30 días?si
¿Querés eliminar automáticamente notificaciones no leídas después de 90 días?si
¿Querés implementar un job de limpieza de notificaciones?puede ser, eso segun tu criterio
¿Querés que los documentos generados NO se borren aunque se borre una notificación asociada?puede ser, eso segun tu criterio
¿Querés generar notificación in-app cuando se aprueba/rechaza un medio de pago?si
¿Querés generar notificación in-app cuando se aprueba/rechaza una consignación?si
¿Querés generar notificación in-app cuando se genera una multa?si
¿Querés generar notificación in-app cuando una subasta inscrita está por iniciar?si
¿Querés evitar notificaciones persistentes por eventos muy frecuentes de pujas?quizas no hace falta, eso segun tu criterio

O) Estadísticas, historial y filtros
Contexto: endpoints existen, pero algunos filtros parecen no estar completos.
¿Querés que estadísticas soporte períodos mes, trimestre, anual y total?si
¿Querés que el filtro periodo afecte realmente los cálculos del backend? puede ser, eso segun tu criterio
¿Querés separar estadísticas como comprador/postor y como vendedor/consignador? si
¿Querés que historial incluya pujas ganadas, perdidas y superadas? si
¿Querés que historial sea paginado? puede ser, eso segun tu criterio
¿Querés que compras se puedan filtrar por estado desde backend?puede ser, eso segun tu criterio
¿Querés que consignaciones se puedan filtrar por activas, rechazadas y vendidas?si

P) Jobs automáticos y vencimientos
Contexto: varias reglas tienen plazos, pero no todas parecen automatizadas.
¿Querés implementar job automático para vencer medios de pago verificados?puede ser, eso segun tu criterio
¿Querés implementar job automático para bloquear cuentas con multas vencidas?puede ser, eso segun tu criterio
¿Querés implementar job automático para marcar compras abandonadas por falta de pago de comisiones/envío?puede ser, eso segun tu criterio
¿Querés implementar job automático para marcar devoluciones vencidas si el usuario no retira ni paga envío?puede ser, eso segun tu criterio
¿Querés implementar job automático para limpiar notificaciones antiguas?puede ser, eso segun tu criterio
¿Querés que, si no hay jobs automáticos para alguna regla, exista endpoint admin para simular/procesar esos vencimientos manualmente?puede ser, eso segun tu criterio

Q) Estados finales y enums
Contexto: hay nombres similares pero no idénticos entre docs y código. Conviene congelar una lista final.
¿Querés publicar enums finales de cuenta, medio de pago, compra, consignación, inscripción, puja y documento?puede ser, eso segun tu criterio
¿Querés que el backend use internamente los mismos nombres de estado que se documenten para frontend?puede ser, eso segun tu criterio
¿Querés evitar traducir estados en frontend y mandar ya estados estables desde backend?puede ser, eso segun tu criterio
¿Querés mantener estados internos más detallados aunque el frontend los agrupe visualmente?puede ser, eso segun tu criterio
¿Querés que pendiente_verificacion sea el nombre final del estado de medio de pago pendiente?si
¿Querés que restriccion_multa sea el estado final para cuenta con multa activa?si
¿Querés que bloqueada_permanente sea el estado final para cuenta bloqueada por multa vencida?si
