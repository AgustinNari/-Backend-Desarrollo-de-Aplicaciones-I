# Material fuente: pantallas_diseno_original.md

> Este archivo conserva el texto original recibido para que Codex tenga acceso a todos los detalles, incluso los que no estén resumidos en la documentación central.

---

Esto que te paso a continuacion seria una vista general pasada a texto de las pantallas realizadas en el diseño de la app, pensa igual que varios de los detalles quizas no son tan precisos, ya que estan pasados por una IA, entonces, no estaria hecho por mi esa conversion de las imagenes a texto (por ej, entre otras cosas, esas cosas que agrego la IA de data ejemplo/data estimada). Basicamente, lo que quiero que tengas es al menos las pantallas en general, junto con el concepto basico de cada una, como para entender al menos de manera general como lo diseñe y pense, pensado lo que ya mencione para los endpoints, que en general, se deberia cambiar/agregar solo lo necesario, aunque obviamente, es mas que nada para cambios grandes, osea obviamente, se pueden cambiar textos y algunas cosas de cada pantalla, y quizas agregar algunas, pero en general, no rediseñar cosas completamente ni cambios demasiado abruptos:
Flujo general de autenticación / onboarding – QuickBid
1. Splash Screen
Pantalla inicial con branding de QuickBid.
Objetivo:
Mostrar logo.
Cargar sesión/configuración inicial.
Redirigir según estado del usuario.

2. Acceso Invitado
Pantalla para usuarios no registrados.
Objetivo:
Permitir navegación limitada como observador.
Contenido principal:
Explicación del modo invitado.
Botón:
“Iniciar Sesión”
“Registrarme”
“Continuar igualmente”
Restricciones del invitado:
No puede pujar.
No puede comprar.
Acceso limitado a funciones protegidas.

LOGIN
3. Login
Endpoint:
POST /api/auth/login
Campos:
Email
Contraseña
Acciones:
Iniciar Sesión
Continuar como Invitado
Olvidé mi contraseña
Registrarte
Resultado esperado:
Generación de sesión/token.
Redirección al home/dashboard.

REGISTRO
4. Registro – Etapa 1: Datos Personales
Endpoint:
POST /api/auth/registro/etapa1
Información solicitada:
Email
Nombre
Apellido
Domicilio legal
País de origen
Acción principal:
“Continuar registro”
Objetivo:
Crear el perfil inicial del usuario.

5. Registro – Etapa 2: Validación de Identidad
Endpoint:
POST /api/auth/registro/etapa2
Datos requeridos:
Foto frontal del DNI
Foto trasera del DNI
Formato esperado:
JPG / PNG / PDF
Objetivo:
Validar identidad del usuario.
Acción principal:
“Completar registro”

6. Validando Datos
Pantalla intermedia de procesamiento.
Mensaje principal:
“Estamos validando tus datos”
Aviso de que se enviará un email cuando finalice la validación.
Objetivo:
Informar que el proceso KYC/verificación está en revisión.

7. Seguridad – Creación de Contraseña
Endpoint:
POST /api/auth/registro/etapa3
Campos:
Nueva contraseña
Confirmar contraseña
Requisitos sugeridos:
Mayúsculas
Número
Longitud mínima
Acción principal:
“Finalizar registro”

8. Verificación de Token / Cuenta
Endpoint:
POST /api/auth/registro/verificar-token
Objetivo:
Confirmar email o enlace de validación.
Posibles validaciones:
Token válido
Token expirado
Usuario ya verificado

ACCESO LIMITADO
9. Acceso Limitado
Pantalla mostrada cuando el usuario aún no completó validaciones obligatorias.
Mensaje:
Para participar en subastas o realizar operaciones, el usuario debe:
Verificar identidad
Agregar método de pago
Acciones:
“Agregar Medio de Pago”
“Continuar como Observador”

RECUPERACIÓN DE CUENTA
10. Recuperación de Cuenta
Endpoint:
POST /api/auth/recuperar-clave
Campo:
Correo electrónico
Acción:
“Enviar enlace de recuperación”
Objetivo:
Enviar email para restablecer contraseña.

11. Nueva Clave
Endpoint:
POST /api/auth/cambiar-clave
Campos:
Nueva contraseña
Repetir contraseña
Acción:
“Actualizar y acceder”
Objetivo:
Guardar nueva contraseña y loguear al usuario.

REENVÍO DE ENLACE DE REGISTRO
12. Solicitud de Enlace para Completar Registro
Endpoint:
POST /api/auth/registro/reenviar-link
Campo:
Email registrado
Acción:
“Enviar enlace de acceso”
Objetivo:
Reenviar enlace para continuar/completar registro pendiente.

Resumen de endpoints detectados
POST /api/auth/login
POST /api/auth/registro/etapa1
POST /api/auth/registro/etapa2
POST /api/auth/registro/etapa3
POST /api/auth/registro/verificar-token
POST /api/auth/registro/reenviar-link
POST /api/auth/recuperar-clave
POST /api/auth/cambiar-clave

Gestión de Métodos de Pago – QuickBid

1. Listado de Métodos de Pago
Pantalla principal de administración de métodos de pago.
Objetivo
Permitir al usuario:
Ver métodos registrados.
Agregar nuevos.
Eliminar métodos existentes.
Definir método principal.
Consultar estado de validación.

Endpoints asociados
GET    /api/usuario/medios-pago
DELETE /api/usuario/medios-pago/{id}
PATCH  /api/usuario/medios-pago/{id}/principal

Información mostrada por cada método
Tarjeta
Tipo: Visa / Mastercard / Amex
Últimos 4 dígitos
Fecha de vencimiento
Estado:
Principal
Pendiente validación
Verificada
Cuenta bancaria
Banco
Alias o CVU parcial
Estado de validación
Cheque certificado
Estado de revisión
Tiempo estimado de validación

Acciones disponibles
Agregar nuevo
Botón:
“Agregar nuevo”
Fijar como principal
Acción contextual:
“Fijar como principal”
Eliminar método
Acción:
“Borrar método”

2. Selección de Tipo de Medio de Pago
Pantalla para elegir qué método desea registrar el usuario.
Opciones disponibles
Crédito / Débito
Visa
Mastercard
Amex
Cuenta Bancaria
Transferencia vía:
CBU
CVU
Cheque Certificado
Depósito físico o validación manual.

Acción principal
Botón:
“Siguiente”

3. Alta de Nueva Tarjeta
Endpoint:
POST /api/usuario/medios-pago

Datos requeridos
Información de tarjeta
Nombre del titular
Número de tarjeta
Fecha de vencimiento
CVV
Configuración
Moneda:
ARS
USD
Tipo
Nacional
Extranjera

Seguridad mostrada
Mensajes:
“Tu información está encriptada”
“Cumplimos estándares PCI-DSS”

Acción principal
“Enviar para verificación”

4. Alta de Cuenta Bancaria
Endpoint:
POST /api/usuario/medios-pago

Datos solicitados
Cuenta
CBU o CVU
Alias bancario (opcional)
Banco
Entidad bancaria
Titularidad
CUIT / CUIL del titular

Configuración adicional
Moneda
ARS
USD
Tipo de banco
Banco Nacional
Banco Extranjero

Validaciones esperadas
Reglas importantes
La cuenta debe estar a nombre del usuario registrado.
Validación de titularidad bancaria.

Acción principal
“Enviar para verificación”

5. Alta de Cheque Certificado
Endpoint:
POST /api/usuario/medios-pago

Objetivo
Permitir carga manual de cheque certificado para operaciones específicas.

Datos requeridos
Información del cheque
Número de cheque
Banco emisor
Monto
Fecha de vencimiento
Archivos
Foto frente del cheque
Foto dorso del cheque
Formatos permitidos
JPG
PNG

Mensajes informativos
El cheque físico deberá entregarse al momento de la subasta.
Proceso sujeto a validación manual.

Acción principal
“Enviar para verificación”

6. Validación de Medio de Pago
Pantalla de estado posterior al alta.
Objetivo
Informar que el método está siendo revisado.

Mensajes principales
Estado
“Validando tu medio de pago”
Información adicional
El equipo revisará la información proporcionada.
Se enviará confirmación por email si la validación es exitosa.

Acción disponible
“Volver a mis métodos”




Perfil y Cuenta de Usuario – QuickBid

1. Menú Lateral
Menú principal de navegación del usuario autenticado.
Información mostrada
Perfil resumido
Nombre y apellido
Nivel de usuario
QuickBid ID
Estado de verificación

Secciones disponibles
Navegación principal
Subastas
Mis compras
Consignación
Notificaciones
Ayuda
Perfil

Acciones adicionales
Cerrar sesión
Botón:
“Cerrar sesión”
Objetivo:
 Finalizar sesión y eliminar token/autenticación activa.

2. Mi Perfil
Endpoint:
 GET /api/usuario/perfil

Objetivo
Centralizar información del usuario y accesos rápidos.

Información principal
Datos del usuario
Nombre completo
Email
Nivel del usuario
Puntos/reputación
Estado:
Oro
Platino
etc.
Progreso de categoría
Barra de progreso
Puntos restantes para subir de nivel

Secciones internas
Actividad
Historial de subastas
Estadísticas
Cuenta
Métodos de pago
Contraseña
Dirección de envío

Datos posibles del perfil
{
 "id": "uuid",
 "nombre": "Nicolas",
 "apellido": "Lussoro",
 "email": "usuario@email.com",
 "nivel": "Platino",
 "puntos": 1250,
 "estadoVerificacion": "verificado",
 "porcentajeNivel": 75
}

3. Estadísticas
Endpoint:
 GET /api/usuario/estadisticas

Objetivo
Mostrar métricas de actividad del usuario dentro de la plataforma.

Métricas observadas
Financieras
Total invertido
Variación respecto al mes anterior
Rendimiento
Tasa de victorias
Puja promedio
Actividad mensual
Gráfico por mes
Evolución histórica
Segmento favorito
Categoría más utilizada
Cantidad de compras
Porcentaje de participación

Posibles métricas adicionales
Cantidad de subastas ganadas
Total consignado
Promedio de gasto mensual
Ranking de usuario
Participación histórica

4. Historial de Subastas
Endpoint:
 GET /api/usuario/historial

Objetivo
Listar actividad histórica de compras y pujas.

Información mostrada por registro
Subasta
Nombre del artículo
Categoría
Número de lote
Resultado
Ganada
Perdida
Adjudicada
Superada
Información económica
Monto final
Fecha de operación

Acciones
“Ver todo el historial”

Posible estructura
{
 "id": "subasta-id",
 "articulo": "Mercedes-Benz 280SL 1970",
 "categoria": "Autos clásicos",
 "lote": "042",
 "estado": "ganada",
 "monto": 43100000,
 "fecha": "2026-03-28"
}

5. Notificaciones
Endpoint:
 GET /api/usuario/notificaciones

Objetivo
Centralizar alertas y eventos relevantes.

Categorías observadas
Filtros
Todo
Subastas
Consignas
Pagos

Tipos de notificación
Subastas
Ganaste una subasta
Nueva puja superadora
Nueva subasta disponible
Consignación
Consignación aprobada
Publicación validada
Pagos
Liquidación acreditada
Pago pendiente
Método validado

Información mostrada
Datos básicos
Título
Descripción breve
Hora o fecha
Estado de lectura

Acción detectada
Marcar como leído
Endpoint:
 PATCH /api/usuario/notificaciones/{id}/leer

Posible estructura
{
 "id": "notif-id",
 "tipo": "subasta",
 "titulo": "Ganaste el lote #042",
 "descripcion": "Mercedes-Benz 280SL adjudicado",
 "leida": false,
 "fecha": "2026-03-28T12:00:00"
}

6. Ayuda y Soporte
Pantalla de centro de ayuda/autogestión.

Objetivo
Brindar soporte y respuestas rápidas.

Funcionalidades
Buscador
Buscar en el centro de ayuda
Categorías frecuentes
Consignaciones
Pujas y subastas
Pagos y cobros
Cuenta y seguridad
Sistema de categorías

Información adicional
Contacto de soporte
Email de ayuda
Ejemplo:
ayuda@quickbid.com

Endpoints detectados
GET   /api/usuario/perfil
GET   /api/usuario/estadisticas
GET   /api/usuario/historial
GET   /api/usuario/notificaciones
PATCH /api/usuario/notificaciones/{id}/leer







Flujo general — Compras / Pagos / Entrega
1. Mis Compras
Pantalla principal de compras del usuario
Endpoint
GET /api/compras
Objetivo
Mostrar todas las compras realizadas por el usuario, agrupadas por estado.
Tabs / estados
Todos
Pendientes
Pagados
(posibles futuros estados)
Cada card de compra muestra
Imagen del producto
Nombre del producto
Lote
Categoría / subasta
Total pagado o pendiente
Estado:
Con multa
Pendiente de pago
Pagado
Tiempo restante (si aplica)
CTA:
“Pagar ahora”
“Completar envío”
Ejemplo de data
{
  "idCompra": 4829,
  "producto": {
    "nombre": "Patek Philippe Calatrava 1950",
    "imagen": "url-imagen"
  },
  "estado": "pendiente_pago",
  "monto": 14500,
  "moneda": "USD",
  "fechaLimitePago": "2024-05-12T18:12:00Z",
  "tieneMulta": true
}

2. Detalle de Entrega y Producto
Pantalla de detalle previo al pago
Endpoint
PUT /api/compras/{id}/entrega
Objetivo
Configurar modalidad de entrega antes de pagar.
Contenido
Imagen del producto
Nombre
Precio adjudicado
Información de la subasta
Selector de modalidad de entrega
Opciones de entrega
Envío a domicilio
Retiro presencial
Courier / logística externa
Data ejemplo
{
  "idCompra": 4829,
  "modalidadEntrega": "domicilio",
  "direccionEntrega": {
    "calle": "Libertador 1234",
    "ciudad": "CABA"
  }
}

3. Resumen de Compra / Pago
Pantalla de checkout
Endpoints
GET /api/compras/{id}
GET /api/usuario/medios-pago
Objetivo
Permitir confirmar el pago de la compra.
Contenido
Resumen del producto
Método de pago seleccionado
Desglose económico:
Oferta ganadora
Comisión
Envío
Impuestos
Total final
Botón:
“Confirmar y pagar ahora”
Data ejemplo
{
  "compra": {
    "id": 4829,
    "producto": "Patek Philippe Calatrava 1950"
  },
  "pago": {
    "metodo": "visa",
    "ultimos4": "4242"
  },
  "costos": {
    "oferta": 14500,
    "comision": 1450,
    "envio": 145,
    "total": 15950
  }
}

4. Confirmación de Compra Exitosa
Pantalla post pago
Endpoint
POST /api/compras/{id}/pagar
Objetivo
Confirmar que el pago fue procesado correctamente.
Contenido
Estado exitoso
Número de orden
Producto comprado
Total pagado
Información de entrega
Fecha estimada de entrega
Ejemplo
{
  "estado": "pagado",
  "orden": "ORD-8924-XT9",
  "fechaEntregaEstimada": "2024-10-14"
}

5. Factura / Documento de Compra
Pantalla de comprobante fiscal
Endpoint
GET /api/compras/{id}/documentos
Objetivo
Mostrar y descargar factura o comprobantes.
Contenido
Número de factura
Estado de pago
Producto
Desglose:
Precio martillo
Comisión
IVA
Envío
Total facturado
Datos del emisor
Datos del receptor
Método de pago
Ejemplo
{
  "factura": {
    "numero": "0001-00004562",
    "estado": "pagada"
  },
  "totales": {
    "subtotal": 15000,
    "iva": 315,
    "envio": 200,
    "total": 17015
  }
}

Flujo de Multas
6. Recibo de Multa
Pantalla de comprobante de multa pagada
Endpoint
GET /api/compras/{id}/documentos
Objetivo
Mostrar comprobante por pago fuera de término.
Contenido
Estado: pagado
Monto abonado
Número de multa
Fecha de vencimiento
Motivo de infracción
Método de pago
ID de transacción
Botón compartir recibo
Ejemplo
{
  "multa": {
    "numero": "MUL-2026-00847",
    "monto": 24500,
    "estado": "pagada"
  }
}

7. Resumen de Pago con Multa
Pantalla de checkout con penalización
Endpoints
GET /api/usuario/medios-pago
POST /api/compras/{id}/pagar-con-multa
Objetivo
Permitir pagar una compra vencida agregando multa automática.
Contenido
Producto
Método de pago
Oferta original
Multa (%)
Total actualizado
CTA:
“Confirmar y pagar ahora”
Ejemplo
{
  "compra": 14500,
  "multa": 1450,
  "total": 15950
}

Endpoints detectados
GET    /api/compras
GET    /api/compras/{id}
PUT    /api/compras/{id}/entrega
POST   /api/compras/{id}/pagar
POST   /api/compras/{id}/pagar-con-multa
GET    /api/compras/{id}/documentos
GET    /api/usuario/medios-pago


Flujo General — Consignaciones

1. TyC Consignación
Pantalla inicial antes de consignar
Objetivo
Aceptar términos y condiciones para iniciar el proceso de consignación.
Contenido
Explicación general del proceso
Requisitos principales
Checkboxes:
Aceptar TyC
Declaración de propiedad del bien
CTA:
“Continuar”
Ejemplo
{
  "aceptaTyC": true,
  "declaraPropiedad": true
}

2. Requisitos para Consignar
Validaciones previas del usuario
Endpoints
GET /api/usuario/medios-pago
GET /api/consignaciones/requisitos
Objetivo
Verificar que el usuario tenga:
Método de pago validado
Cuenta bancaria
Documentación básica
Estados posibles
Completo
Pendiente
Requiere validación
Ejemplo
{
  "usuario": {
    "medioPagoValidado": true,
    "cuentaBancaria": true,
    "documentacion": false
  }
}

3. Datos del Bien
Carga de información del objeto a consignar
Objetivo
Registrar información principal del bien.
Contenido
Fotos del bien
Título
Marca / modelo
Descripción
Categoría
Estado
Año / referencia
Ejemplo
{
  "titulo": "Reloj Cartier Santos 1978",
  "categoria": "Relojería",
  "descripcion": "Modelo vintage en excelente estado",
  "imagenes": [
    "img1.jpg",
    "img2.jpg"
  ]
}

4. Detalle del Objeto
Información técnica y documentación
Objetivo
Completar metadata y trazabilidad del bien.
Contenido
Origen del bien
Fecha estimada
Autor / artista / diseñador
Historial
Observaciones
Estado de conservación
Ejemplo
{
  "origen": "Colección privada",
  "autor": "Cartier",
  "estado": "Excelente",
  "historial": "Único dueño"
}

5. Documentación de Origen
Carga de comprobantes y certificados
Endpoint
POST /api/consignaciones/{id}/documentacion-origen
Objetivo
Subir documentación respaldatoria.
Documentos posibles
Factura
Certificado de autenticidad
Recibos
Notas adicionales
Ejemplo
{
  "documentos": [
    {
      "tipo": "certificado_autenticidad",
      "archivo": "certificado.pdf"
    }
  ]
}

6. Solicitud Enviada
Confirmación de envío de consignación
Endpoint
POST /api/consignaciones
Objetivo
Informar que la solicitud fue registrada.
Contenido
Número de consignación
Estado inicial
Próximos pasos
Tiempo estimado de revisión
Ejemplo
{
  "numero": "CONS-2020-00847",
  "estado": "en_revision"
}

7. Mis Consignaciones
Listado general de consignaciones
Endpoint
GET /api/consignaciones
Tabs
Activas
Rechazadas
Vendidas
Cada card muestra
Producto
Precio estimado
Estado
Ver detalle
Estados comunes
En revisión
Verificación física
Acuerdo pendiente
En subasta
Vendido
Rechazado

8. Detalle Consignación Activa
Seguimiento completo de la consignación
Endpoint
GET /api/consignaciones/{id}
Objetivo
Visualizar el workflow completo.
Etapas
Validación
Verificación física
Acuerdo confirmado
En subasta
Liquidación
Acciones posibles
Ver acuerdo
Ver póliza
Ver liquidación
Ejemplo
{
  "estado": "acuerdo_pendiente",
  "precioBase": 2800000
}

9. Revisión Física Rechazada
Resultado negativo de inspección
Endpoint
GET /api/consignaciones/{id}
Objetivo
Mostrar motivo del rechazo.
Contenido
Estado rechazado
Motivo
Observaciones del especialista
Posibles daños o inconsistencias
Ejemplo
{
  "estado": "rechazada_revision",
  "motivo": "Artículo dañado"
}

10. Acuerdo Rechazado
Usuario rechaza condiciones
Endpoint
GET /api/consignaciones/{id}
Objetivo
Informar que el acuerdo no fue aceptado.
Contenido
Estado final
Mensaje explicativo
Gestión de devolución

11. Revisión Acuerdo Pendiente
Pantalla de decisión sobre el acuerdo
Endpoint
POST /api/consignaciones/{id}/acuerdo/rechazar
Objetivo
Aceptar o rechazar el acuerdo comercial.
Contenido
Comisión
Precio base
Condiciones
Botones:
“Aceptar acuerdo”
“Rechazar acuerdo”
Ejemplo
{
  "precioBase": 2800000,
  "comision": 10
}

12. Confirmar Acuerdo
Confirmación previa a aceptar
Endpoint
POST /api/consignaciones/{id}/acuerdo/aceptar
Objetivo
Validar aceptación final del consignador.
Contenido
Resumen contractual
Checkboxes legales
Confirmación final

13. Acuerdo Aceptado
Contrato confirmado
Objetivo
Mostrar acuerdo ya firmado/aceptado.
Contenido
Número de contrato
Producto
Comisión
Fecha
Estado aceptado
Acción compartir
Ejemplo
{
  "contrato": "CON-2023-001",
  "estado": "aceptado"
}

14. Bien Consignado
Ficha pública del bien
Objetivo
Mostrar el bien activo en consignación/subasta.
Contenido
Imagen principal
Valor asegurado
Estado
Vigencia
Ubicación
Cobertura principal
Ejemplo
{
  "estado": "asegurado",
  "valor": 150000
}

15. Liquidación de Venta
Pantalla posterior a venta exitosa
Objetivo
Mostrar liquidación económica final.
Contenido
Precio final de venta
Comisión empresa
Neto a cobrar
Cuenta bancaria destino
Fecha acreditación
Ejemplo
{
  "precioVenta": 3450000,
  "comision": 517500,
  "neto": 2870000
}

16. Gestión de Devolución
Opciones para recuperar un bien
Endpoint
POST /api/consignaciones/{id}/devolucion
Objetivo
Seleccionar cómo devolver el objeto.
Opciones
Retiro en sucursal
Envío a domicilio
Definir plazo de retiro
Ejemplo
{
  "tipoDevolucion": "retiro_sucursal"
}

17. Resumen de Pago de Devolución
Checkout para pagar logística/envío
Endpoints
GET /api/usuario/medios-pago
GET /api/usuario/perfil
POST /api/consignaciones/{id}/devolucion/pagar-envio
Objetivo
Pagar costo de devolución/envío.
Contenido
Método de pago
Resumen económico
Costo de envío
Total
Ejemplo
{
  "envio": 1450,
  "total": 1450
}

18. Comprobante de Pago
Confirmación de pago de devolución
Objetivo
Mostrar comprobante exitoso.
Contenido
Estado pago
Monto abonado
Método de pago
Dirección de entrega
Botón compartir
Ejemplo
{
  "estado": "pagado",
  "monto": 1450
}

Endpoints Detectados
GET    /api/consignaciones
GET    /api/consignaciones/{id}

POST   /api/consignaciones
POST   /api/consignaciones/{id}/documentacion-origen

POST   /api/consignaciones/{id}/acuerdo/aceptar
POST   /api/consignaciones/{id}/acuerdo/rechazar

POST   /api/consignaciones/{id}/devolucion
POST   /api/consignaciones/{id}/devolucion/pagar-envio

GET    /api/usuario/medios-pago
GET    /api/usuario/perfil
GET    /api/consignaciones/requisitos


Flujo general de las subastas (QuickBid)
1. Subastas Activas
Endpoint: GET /api/subastas
Pantalla principal donde el usuario ve:
Subastas activas
Próximas subastas
Filtros por categoría:
Joyas
Arte
Vehículos
Plata
Oro
etc.
Cards de subastas con:
Imagen destacada
Nombre de la subasta
Rematador
Moneda
CTA:
“Entrar”
“Ver más”
Data principal
{
  "id": "sub_001",
  "titulo": "Subasta Anual de Clásicos",
  "categoria": "Vehículos",
  "moneda": "USD",
  "estado": "Activa",
  "rematador": "Renatas Centenario",
  "imagen": "cover.jpg"
}

DETALLE DE SUBASTA
2. Detalle de Colección / Subasta
Endpoint: GET /api/subastas/{id}
Pantalla con información completa de una subasta.
Contenido
Nombre de la colección
Tipo de colección
Fecha
Hora
Ubicación
Rematador
Categoría
Segmento
Moneda
Acciones
Inscribirse
Entrar al catálogo
Entrar a puja en vivo
Data ejemplo
{
  "id": "sub_001",
  "titulo": "Colección Vanguardia",
  "fecha": "2023-10-24",
  "hora": "18:00 GMT-5",
  "ubicacion": "Grosvenor Square, Londres",
  "modalidad": "Virtual",
  "categoria": "Plata",
  "segmento": "Arte & Diseño",
  "moneda": "USD"
}

INSCRIPCIÓN A SUBASTA
3. Selección de Método de Pago
Endpoints:
GET /api/usuario/medios-pago
POST /api/subastas/{id}/inscribirse
Pantalla para registrarse en una subasta utilizando un medio de pago validado.
Funcionalidad
Mostrar métodos disponibles
Seleccionar uno
Validación de fondos o disponibilidad
Confirmar inscripción
Métodos soportados
Visa
Cuenta bancaria
Otros medios
Data ejemplo
{
  "metodosPago": [
    {
      "id": "mp_01",
      "tipo": "Visa",
      "ultimos4": "1009",
      "moneda": "USD",
      "estado": "activo"
    }
  ]
}

4. Confirmación de Inscripción
Pantalla de éxito luego de inscribirse.
Contenido
Estado exitoso
Resumen:
Subasta
Método de pago
Mensaje informativo:
La validación puede demorar hasta 24 hs
Respuesta ejemplo
{
  "success": true,
  "estado": "pendiente_validacion",
  "subasta": "Colección Vanguardia"
}

CATÁLOGO
5. Catálogo Secuencial
Endpoint: GET /api/subastas/{id}/catalogo
Listado de lotes/items de una subasta.
Contenido
Lista vertical de lotes
Imagen
Título
Estado:
Finalizado
En vivo
Próximo
Precio actual
Navegación secuencial
Data ejemplo
{
  "items": [
    {
      "id": "lot_026",
      "titulo": "Escultura Vive",
      "estado": "EN VIVO",
      "precioActual": 4200
    }
  ]
}

DETALLE DE ITEM
6. Detalle de Item
Endpoint: GET /api/items/{id}
Pantalla individual del lote.
Contenido
Imagen principal
Galería
Nombre del item
Número de lote
Categoría
Precio base
Cantidad de pujas
Autor / artista
Tabs:
Detalles
Historia
Datos de interés
Data ejemplo
{
  "id": "lot_12841",
  "titulo": "Fragmentos de Eternidad N°12",
  "precioBase": 3000,
  "pujasTotales": 18,
  "autor": "Elena Velázquez"
}

PUJA EN VIVO
7. Sala de Subasta / Live Auction
Endpoint: GET /api/subastas/{id}/puja-actual
Pantalla principal de puja en tiempo real.
Contenido
Lote actual
Imagen destacada
Precio actual
Mejor oferta
Temporizador
Historial reciente
Estado EN VIVO
Acciones
Pujar ahora
Ver catálogo
Ver detalle
Data ejemplo
{
  "loteActual": {
    "id": "lot_001",
    "titulo": "Porsche 911 Carrera GTS",
    "precioActual": 145500,
    "mejorOferta": 142000,
    "estado": "EN_VIVO"
  }
}

8. Modal de Oferta / Confirmación de Puja
Endpoint: POST /api/subastas/{id}/pujar
Modal para confirmar la oferta antes de enviarla.
Contenido
Monto de oferta
Incrementos mínimos/máximos
Método de pago
Confirmar puja
Payload ejemplo
{
  "subastaId": "sub_001",
  "itemId": "lot_001",
  "monto": 150000,
  "medioPagoId": "mp_01"
}

9. Estado “Enviando Puja”
Pantalla/loading intermedio.
Funcionalidad
Bloquea interacción
Espera confirmación backend
Validación de oferta
Estado ejemplo
{
  "estado": "procesando_puja"
}

10. Puja Exitosa
Pantalla final luego de ganar o confirmar la puja.
Contenido
Mensaje de éxito
Monto ganador
Item ganado
CTA:
Ir a completar compra
Volver a subasta
Respuesta ejemplo
{
  "success": true,
  "resultado": "PUJA_GANADA",
  "montoFinal": 159000
}

Endpoints detectados
GET    /api/subastas
GET    /api/subastas/{id}
GET    /api/subastas/{id}/catalogo
GET    /api/subastas/{id}/puja-actual
GET    /api/items/{id}

GET    /api/usuario/medios-pago

POST   /api/subastas/{id}/inscribirse
POST   /api/subastas/{id}/pujar


Algunos Estados, Alertas y Posibles Errores — QuickBid
Estas pantallas complementan el flujo principal de subastas y cubren:
restricciones de cuenta,
errores de red,
validaciones de puja,
límites financieros,
fallos de pago,
alertas operativas.

1. Cuenta Parcialmente Restringida
Pantalla de advertencia para usuarios con restricciones parciales.
Objetivo
Informar que ciertas funcionalidades fueron bloqueadas temporalmente.
Contenido
Datos del usuario
Estado de cuenta:
“Cuenta parcialmente restringida”
Motivo de restricción
Funciones bloqueadas
CTA:
Regularizar situación
Hablar con soporte
Funciones posiblemente bloqueadas
Participar en nuevas subastas
Realizar pujas
Confirmar compras
Data ejemplo
{
  "usuario": {
    "nombre": "Ana Morales",
    "email": "ana.m@gmail.com"
  },
  "estadoCuenta": "PARCIALMENTE_RESTRINGIDA",
  "motivo": "Pago pendiente ARS 340.000",
  "funcionesBloqueadas": [
    "PUJAR_EN_SUBASTAS"
  ]
}

2. Cuenta Bloqueada / Funciones Limitadas
Estado más severo donde la cuenta queda inutilizable para operaciones críticas.
Contenido
Estado:
“Cuenta bloqueada”
Motivo legal o financiero
Restricción total de funcionalidades
CTA soporte
Casos posibles
Mora prolongada
Fraude
Incumplimiento contractual
Caso judicial
Data ejemplo
{
  "estadoCuenta": "BLOQUEADA",
  "motivo": "Caso derivado a instancia judicial",
  "bloqueos": [
    "PUJAR",
    "COMPRAR",
    "INSCRIBIRSE"
  ]
}

3. Modal — Uso de Datos Móviles
Modal preventivo antes de ingresar a experiencias en tiempo real.
Objetivo
Advertir consumo elevado de datos móviles.
Contenido
Mensaje de advertencia
Explicación:
streaming,
actualizaciones en vivo,
sincronización constante.
Opciones:
“Usar sólo con Wi-Fi”
“Continuar de todos modos”
Data ejemplo
{
  "tipo": "AVISO_DATOS_MOVILES",
  "requiereConfirmacion": true
}

4. Sin Conexión a Internet
Pantalla offline.
Objetivo
Manejar pérdida de conectividad durante una operación.
Contenido
Estado sin internet
Explicación:
progreso guardado localmente
CTA:
Guardar y continuar localmente
Cancelar y volver
Funcionalidad esperada
Persistencia local temporal
Reintento automático
Sincronización al recuperar señal
Data ejemplo
{
  "estado": "OFFLINE",
  "modo": "LOCAL_CACHE"
}

5. Puja Superada
Alerta cuando otro usuario oferta un monto mayor antes de confirmar.
Objetivo
Notificar que la oferta quedó desactualizada.
Contenido
Item actual
Oferta del usuario
Nueva oferta líder
Diferencia
CTA:
Pujar de nuevo
Volver a la puja activa
Data ejemplo
{
  "estado": "PUJA_SUPERADA",
  "itemId": "lot_108",
  "miOferta": 150000,
  "ofertaActual": 153000
}

6. Error — Puja Rechazada
Pantalla de fallo general al procesar una puja.
Posibles causas
Oferta superada antes de confirmación
Problemas de red
Timeout
Conflicto de sincronización
Contenido
Mensaje de error
Lista de posibles causas
CTA:
Realizar nueva puja
Data ejemplo
{
  "success": false,
  "codigo": "BID_REJECTED",
  "motivo": "CONFLICTO_DE_ESTADO"
}

7. Límite de Puja Alcanzado
Pantalla financiera/validación.
Objetivo
Bloquear ofertas que excedan el límite validado del usuario.
Contenido
Intento de puja
Límite permitido
Diferencia excedida
Medio de pago asociado
CTA:
Cambiar medio de pago
Volver a subasta
Validaciones
Score financiero
Fondos disponibles
Límite de tarjeta
Garantías previas
Data ejemplo
{
  "estado": "LIMITE_EXCEDIDO",
  "montoIntentado": 125000,
  "limiteActual": 87000,
  "excedente": 38000,
  "medioPago": "Visa Platinum"
}

8. Pago Fallido
Pantalla post-subasta cuando el cobro no pudo procesarse.
Objetivo
Informar deuda pendiente y consecuencias.
Contenido
Item ganado
Monto de puja
Multa aplicable
Tiempo límite de regularización
Advertencia:
bloqueo temporal/permanente
CTA:
Ir a compras para pagar
Posibles causas
Fondos insuficientes
Tarjeta rechazada
Timeout bancario
Fraude preventivo
Data ejemplo
{
  "estado": "PAGO_FALLIDO",
  "item": "Porsche 911 Carrera GTS",
  "monto": 159000,
  "multa": 15900,
  "plazoHoras": 72
}































Algunos Otros Flujos Adicionales — Validación, Acceso y Consignación

1. Acceso Limitado para Invitados
Pantalla: acceso restringido / usuario no autenticado
Objetivo
Informar que ciertas funcionalidades requieren cuenta verificada e inicio de sesión.
Contexto
Se muestra cuando:
el usuario no inició sesión
intenta pujar
intenta consignar
intenta acceder a funcionalidades privadas
Mensaje principal
Acceso Limitado para Invitados
Información mostrada
Para poder:
realizar pujas
ver precios
consignar bienes
utilizar funciones avanzadas
el usuario necesita:
cuenta registrada
cuenta verificada
sesión iniciada
Acciones disponibles
Iniciar Sesión
Registrarme
Seguir Explorando
Estados posibles
{
  "usuario_autenticado": false,
  "usuario_verificado": false,
  "modo": "guest"
}

2. Subasta Bloqueada por Método No Validado
Pantalla: GET /api/subastas/{id}
Objetivo
Bloquear la participación cuando el usuario no tiene un método válido para pujar.
Contexto
El usuario:
está autenticado
puede navegar el catálogo
pero no puede participar en vivo
Información mostrada
Datos generales de la subasta
Estado de acceso restringido
Alerta principal
No tienes método validado o acorde para pujar en esta subasta
Acciones habilitadas
Entrar a Catálogo
Acciones bloqueadas
Pujar
Participar en vivo
Reglas posibles
tarjeta no validada
moneda incompatible
límite insuficiente
verificación pendiente
Data estimada
{
  "subasta_id": "sub_001",
  "usuario_habilitado_para_pujar": false,
  "motivo": "metodo_pago_invalido",
  "catalogo_disponible": true
}

3. Validación Física Pendiente (Consignación)
Pantalla: flujo de consignación
Objetivo
Mostrar el estado del proceso de validación física de un artículo consignado.
Contexto
El usuario ya:
registró el artículo
inició proceso de consignación
pasó validación inicial
Estado actual
Pendiente de revisión física
Timeline / etapas
Validación
Revisión física
Acuerdo
En subasta
Liquidación
Información relevante
fecha de validación
estado aprobado
instrucciones para envío físico
Mensaje destacado
Validación física necesaria
Acción principal
Consignar otro bien
Posibles requisitos
envío a depósito
inspección presencial
autenticación
verificación de estado
Data estimada
{
  "item_id": "item_2026_00847",
  "estado": "revision_fisica_pendiente",
  "validacion_previa": "aprobada",
  "siguiente_paso": "envio_deposito"
}

4. Documentación Adicional Pendiente
Pantalla: validación documental
Objetivo
Solicitar documentación adicional antes de aprobar una consignación.
Contexto
La plataforma detectó:
falta de documentación
inconsistencias
necesidad de validación extra
Estado actual
Pendiente de nueva documentación
Timeline / etapas
Validación
Revisión física
Acuerdo
En subasta
Liquidación
Mensaje destacado
Documentación adicional necesaria
Ejemplos de documentación requerida
factura de compra
certificado de autenticidad
documentación legal
imágenes adicionales
comprobante de propiedad
Acción principal
Continuar
Posibles outcomes
aprobación
rechazo
nueva solicitud de documentación
Data estimada
{
  "item_id": "item_2026_00847",
  "estado": "documentacion_pendiente",
  "documentos_requeridos": [
    "certificado_autenticidad",
    "factura_compra"
  ]
}

