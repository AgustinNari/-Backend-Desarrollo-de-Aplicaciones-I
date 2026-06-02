# Checklist Final QuickBid

Revisión integral realizada contra `docs/09_checklist_entrega.md`.

Estados:

- `completo`: implementado y cubierto por verificación o tests.
- `parcial`: usable, con una mejora concreta pendiente.
- `pendiente`: falta implementar.
- `no aplica`: fuera del alcance definido.

## Base Y Arquitectura

| Ítem | Estado | Evidencia o nota |
| --- | --- | --- |
| Proyecto compila | completo | `.\mvnw.cmd clean test` |
| Migraciones crean tablas legacy y nuevas | completo | Flyway `V1` a `V7` validado sobre PostgreSQL vacío |
| No se modifican tablas legacy más allá de crearlas | completo | Extensiones posteriores usan tablas `app_*`; el seed inserta escenarios legacy válidos |
| Seed base y demo disponibles | completo | `V3__seed_base.sql` y `V4__seed_demo_data.sql` |
| Estructura clara | completo | Controllers, services, repositories, entities, DTOs, security, websocket, audit y storage |
| README con pasos para correr y probar | completo | `README.md` |

## Auth Y Registro

| Ítem | Estado | Evidencia o nota |
| --- | --- | --- |
| Etapa 1 | completo | HTTP + validación país/email |
| Etapa 2 con upload | completo | Upload seguro DNI con firma binaria |
| Aprobación/rechazo admin | completo | Motivo persistido y categoría inicial |
| Token de setup | completo | Hash SHA-256, vigencia y un solo uso |
| Etapa 3 | completo | BCrypt y emisión de sesión |
| Login | completo | JWT HS256 |
| Refresh/session | completo | Refresh hasheado, revocable y rotativo |
| Recuperación/cambio de clave | completo | Token temporal hasheado |
| Reenvío de link genérico | completo | Respuesta no enumerable + rate limit |
| Email de auth | completo | SMTP configurable; fake seguro por defecto; tokens planos no se persisten ni imprimen |
| Cuenta bloqueada/restringida | completo | `403` para bloqueo; multa permite navegación pero no puja |

## Usuario

| Ítem | Estado | Evidencia o nota |
| --- | --- | --- |
| Perfil | completo | DTO propio |
| Categoría, puntos y progreso | completo | Tabla configurable y sincronización legacy |
| Estadísticas | completo | Métricas agregadas |
| Historial | completo | Paginado |
| Notificaciones y marcar leído | completo | Ownership validado |
| Dirección de envío | completo | Alta/actualización principal |

## Medios De Pago

| Ítem | Estado | Evidencia o nota |
| --- | --- | --- |
| Listado | completo | Solo propios |
| Alta tarjeta | completo | No persiste PAN ni CVV |
| Alta cuenta | completo | Identificadores hasheados |
| Alta cheque con fotos | completo | Multipart y firma binaria |
| Verificación/rechazo admin | completo | Vigencia 5 días hábiles, puntos, notificación y motivo |
| Principal | completo | Uno por moneda |
| Eliminación lógica | completo | Impide borrar medios vinculados |
| Validación de moneda y estado | completo | Servicio y tests |

## Subastas

| Ítem | Estado | Evidencia o nota |
| --- | --- | --- |
| Listado público/invitado sin precios | completo | DTO reducido |
| Detalle público/invitado sin precios/live | completo | DTO reducido |
| Catálogo público/invitado sin precios | completo | DTO reducido |
| Detalle ítem | completo | Público y autenticado |
| Usuario registrado ve precio base | completo | DTO autenticado |
| Inscripción | completo | Moneda, categoría, estado y plazo |
| Verificación de acceso | completo | Diferencia inscripción y permiso de puja |
| Puja actual para autenticado habilitado para navegar | completo | Snapshot versionado con flag `puedePujar` |
| Pujar con validaciones | completo | Límites, fondos, moneda, categoría e idempotencia |
| Concurrencia protegida | completo | Lock transaccional y tests simultáneos |
| WebSocket/eventos | completo | Autorizacion inbound por topic, filtro outbound por estado actual, colas privadas, heartbeat y presencia con TTL |
| Cierre de lote y compra | completo | Transaccional e idempotente |
| Compra interna empresa si nadie puja | completo | Modelada en `app_compras` |

## Compras Y Pagos

| Ítem | Estado | Evidencia o nota |
| --- | --- | --- |
| Listado compras | completo | Solo propias |
| Detalle compra | completo | Ownership `403` |
| Pago compra | completo | Extras idempotentes |
| Falla de pago genera multa | completo | 10% y restricción |
| Pago con multa | completo | Reactiva si no quedan multas |
| Bloqueo por vencimiento | completo | `bloqueada_permanente` |
| Entrega/envío/retiro | completo | Cobertura y costos |
| Documentos/comprobantes | completo | Listado de metadata autorizada |

## Consignación

| Ítem | Estado | Evidencia o nota |
| --- | --- | --- |
| Requisitos | completo | Cuenta bancaria y medio de pago registrados, no rechazados ni eliminados; la verificación vigente no bloquea el inicio |
| Crear solicitud con mínimo 6 fotos | completo | No crea legacy prematuramente |
| Documentación de origen | completo | PDF/imágenes con firma binaria y revisión manual |
| Listado/detalle | completo | Ownership |
| Revisión/admin | completo | Digital, documental y física |
| Validación de consignador/duenios | completo | Una fila por persona |
| Propuesta de acuerdo | completo | Requiere dueño validado |
| Aceptar/rechazar acuerdo | completo | Transiciones validadas |
| Crear producto legacy solo con acuerdo aceptado | completo | Servicio transaccional |
| Asignar a subasta/catálogo | completo | Ítem y póliza |
| Devolución y pago de envío | completo | Retiro sin cobro; envío idempotente con medio verificado y `verificado_hasta` vigente al pagar |
| Liquidación | completo | No duplica liquidaciones; cuenta bancaria destino registrada y no eliminada, sin exigir verificación manual vigente |

## Seguridad Y Calidad

| Ítem | Estado | Evidencia o nota |
| --- | --- | --- |
| DTOs separados; no devolver entidades | completo | DTOs públicos, autenticados y admin |
| Manejo global de errores | completo | Negocio, Bean Validation, multipart, parámetros y JSON malformado |
| JSON uniforme | completo | También para Spring Security `401/403` |
| Bean Validation + reglas de servicio | completo | Validaciones estructurales y de dominio |
| Auditoría | completo | Auth, pagos, pujas, consignaciones y admin |
| Logs útiles | parcial | Errores, SMTP y mail simulado sin secretos; falta estrategia operativa estructurada |
| Emails críticos | completo | SMTP opcional para auth y eventos seleccionados; notificación interna preservada ante falla post-commit |
| Tests unitarios de reglas críticas | parcial | Hay unitarios de validación y publishers; la mayor parte vive en integración |
| Tests de integración para flujos principales | completo | Auth, usuario, pagos, subastas, concurrencia, compras, consignaciones y admin |

## Seguridad Revisada

- `/api/admin/**` exige header interno solo cuando `APP_ADMIN_ENABLED=true`.
- Un Bearer de usuario normal no otorga permisos admin.
- Passwords usan BCrypt.
- Refresh, setup y recuperación se persisten como hash. El token plano solo se usa en memoria para construir el email.
- Tarjetas no guardan PAN completo ni CVV.
- Recursos privados validan ownership.
- Archivos validan tamaño, MIME declarado y firma binaria mínima.
- El perfil `dev` no contiene claves PostgreSQL ni JWT hardcodeadas.

## Transacciones Críticas Revisadas

Están protegidas con `@Transactional` o delegan a un servicio transaccional:

- aprobación y finalización de registro;
- verificación de medio de pago;
- inscripción y puja;
- cierre de lote/subasta;
- pago automático, extras, multa y vencimiento;
- aceptación de acuerdo y creación de producto legacy;
- asignación a catálogo/subasta y póliza;
- liquidación;
- acciones admin que mutan estado.

## Pendientes No Bloqueantes Para Entrega Académica

| Pendiente | Impacto | Bloquea entrega | Siguiente paso |
| --- | --- | --- | --- |
| Presencia STOMP distribuida | La presencia actual vive en memoria por instancia | No para una instancia | Migrar TTL de sesiones a Redis para despliegue horizontal |
| Pasarela bancaria real | Hoy los cobros son simulados | No; fuera de alcance | Implementar adapter externo idempotente |
| Rate limit distribuido | Necesario con múltiples instancias | No para una instancia | Migrar contadores a Redis |
| Descarga autorizada de archivos | Metadata disponible, binario no expuesto | No para flujos actuales | Agregar endpoint con ownership o URLs firmadas |
| Logging estructurado y observabilidad | Mejora operación productiva | No | Definir correlación, métricas y política de retención |
| Más unit tests puros | Facilita mantenimiento fino | No; integración cubre reglas críticas | Extraer políticas de monto/categoría a componentes testeables |
| APIs deprecadas advertidas por `javac` | Deuda de mantenimiento en `PurchaseService` y un test de pujas | No | Compilar con `-Xlint:deprecation` y migrar llamadas en una tarea separada |

## No Aplica

- Streaming de video de la subasta: fuera del alcance acordado.
- Frontend admin formal: fuera del alcance; se provee backoffice HTTP auxiliar.
