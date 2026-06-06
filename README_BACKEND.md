> **Fuente final:** para decisiones finales usar `docs/00_decisiones_finales.md`, `docs/API_CONTRATO_FINAL.md` y `docs/10_plan_correcciones_backend.md`. Si este README contradice esos archivos, debe actualizarse.


# QuickBid Backend — README para implementación con Codex

## Qué se debe construir

Backend completo para una app móvil de subastas presenciales con participación online. La app permite:

- navegar subastas como invitado con restricciones fuertes;
- registrar usuarios con validación de identidad y aprobación manual;
- iniciar sesión y mantener sesión;
- gestionar medios de pago;
- participar en subastas dinámicas ascendentes en tiempo real;
- realizar pujas válidas según categoría, moneda, límites y fondos;
- gestionar compras, pagos, multas, entrega/retiro y documentos;
- consignar bienes propios para futuras subastas;
- consultar perfil, categoría, puntos, historial, estadísticas y notificaciones.

## Principios de diseño

1. **Fidelidad al diseño previo:** mantener los endpoints públicos definidos y los flujos de pantalla lo más cerca posible del diseño original.
2. **Backend como autoridad:** toda regla de negocio crítica debe validarse en backend, aunque el frontend también la replique para UX.
3. **Legacy intocable:** las tablas existentes se crean si no existen, pero no se modifican ni se les agregan columnas, enums o restricciones nuevas.
4. **Extensión por tablas nuevas:** cualquier necesidad nueva se modela con tablas propias de la app.
5. **Trazabilidad:** registrar auditoría de aprobaciones, rechazos, pujas, pagos, cambios de estado, documentos y acciones admin.
6. **Simplicidad razonable:** si un detalle no está definido, resolverlo con una decisión simple, sólida y documentada.

## Stack recomendado

- Java 17 o superior.
- Spring Boot 3.x.
- Maven.
- Spring Web, Validation, Security, Data JPA.
- JWT con access token y refresh token persistido/revocable.
- PostgreSQL.
- Flyway para migraciones.
- WebSocket/STOMP para subastas en vivo.
- Multipart + BLOB para imágenes y documentos.
- Postman para prueba de endpoints.

## Decisiones asumidas para completar huecos

Estas decisiones se pueden cambiar, pero dan un punto de partida consistente:

- Registro pendiente: expira a las 24 horas desde la última actividad.
- Token de finalización de registro: un solo uso, 48 horas.
- Tokens de recuperación de contraseña: un solo uso, 30 minutos.
- Access token: duración corta, por ejemplo 15 a 60 minutos.
- Refresh token: duración mayor, por ejemplo 7 a 30 días, revocable.
- Password: mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un símbolo.
- DNI/documentos: imágenes JPG/PNG/WebP; no PDF para DNI, salvo que se decida admitirlo explícitamente. Cheques: JPG/PNG.
- Tamaño de archivo sugerido: 10 MB por imagen; configurable.
- Monedas soportadas: `ARS` y `USD`.
- Categorías: `comun`, `especial`, `plata`, `oro`, `platino`.
- Orden de categoría: común < especial < plata < oro < platino.
- Puntos sugeridos: común 0-249, especial 250-699, plata 700-1499, oro 1500-2999, platino 3000+. Ajustable por configuración/tabla.
- Comisión de compra y consignación: modelar en tablas nuevas; no hardcodear en tablas legacy.
- Usuario comprador interno de la empresa: necesario para registrar compra por precio base cuando nadie puja.

## Cómo correr el backend esperado

Codex debería dejar estos pasos claros y funcionando:

```bash
mvn clean install
mvn spring-boot:run
```

Variables/configuración esperadas:

```properties
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
app.jwt.secret=
app.files.storage-path=./uploads
app.mail.enabled=false
```

Para desarrollo, se puede proveer `application-dev.properties` con datos semilla y mail simulado en logs.

## Datos semilla mínimos

- Países principales, incluyendo Argentina y países suficientes para selector.
- Empleados verificadores y revisores.
- Sectores básicos: validación de usuarios, pagos, consignaciones, subastas.
- Subastadores/martilleros demo.
- Usuario aprobado demo con contraseña conocida para pruebas.
- Usuario pendiente, usuario con restricción por multa y usuario bloqueado permanente.
- Medios de pago verificados y pendientes.
- Subastas demo en ARS/USD y varias categorías.
- Catálogos con ítems, imágenes de prueba y precios base para usuarios registrados.
- Consignaciones demo en estados clave.

## Recomendación de pruebas manuales

- Probar flujo invitado: ve subastas/catálogos/detalles sin precios ni puja actual.
- Probar registro: etapa1, etapa2, aprobación admin, verificar token, etapa3, login.
- Probar medios de pago: alta, verificación admin, principal, eliminación lógica.
- Probar subasta: inscripción, validaciones de categoría/moneda/pago, puja válida, puja inválida, concurrencia.
- Probar compra: pago exitoso, entrega por envío, retiro, documentos.
- Probar multa: falla de pago, restricción, pago con multa, bloqueo si vence.
- Probar consignación: carga, documentación, revisión, acuerdo, aceptación, publicación, devolución.

## Material original completo

La carpeta `source_material/` contiene los textos originales completos recibidos. No borrarla: sirve como fuente de verdad para Codex y para revisar detalles no resumidos.
