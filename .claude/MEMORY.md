# Memoria del proyecto — Backend QuickBid

## Criterio general

La documentación de endpoints es la **fuente de verdad**.
Cambios solo con justificación técnica real y aprobación previa.
**Sin sobre-ingeniería** — nivel universitario, código limpio y directo.

---

## Stack

- **Framework**: Spring Boot 3.2.5 (Java 17)
- **Build**: Maven
- **BD**: H2 en archivo (`./quickbiddb`) — migrar a PostgreSQL en producción
- **Seguridad**: Spring Security + JWT (jjwt 0.11.5)
- **Extras**: Lombok, Validation
- **Pendientes comentados**: spring-boot-starter-mail, spring-boot-starter-websocket

---

## Estructura de paquetes

```
com.example.quickbid.quickbid
├── common/
│   ├── dto/ApiResponse.java          ← wrapper { data, message, errors? }
│   ├── exception/
│   │   ├── AppException.java         ← excepción de negocio reutilizable
│   │   └── GlobalExceptionHandler.java
│   ├── security/
│   │   ├── JwtUtil.java              ← genera y valida tokens
│   │   ├── JwtFilter.java            ← OncePerRequestFilter
│   │   └── SecurityConfig.java       ← rutas públicas vs protegidas
│   └── DataSeeder.java               ← datos de prueba (perfil !prod)
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── RegistroTemporal.java
│   ├── RegistroTemporalRepository.java
│   └── dto/  (Etapa1/2/3Request, VerificarToken, Login, RecuperarClave, CambiarClave, ReenviarLink)
├── usuario/
│   ├── Usuario.java
│   └── UsuarioRepository.java
├── subasta/
├── compra/
└── consignacion/
```

---

## Convenciones de la API

- Todos los endpoints excepto `/api/auth/**` requieren Bearer token
- Respuesta estándar: `{ data, message, errors? }` → implementado en `ApiResponse<T>`
- Códigos HTTP estándar según la docu

---

## Decisiones de diseño — Auth

### 1. `RegistroTemporal` como tabla de paso
Se usa para almacenar el progreso del registro en sus 3 etapas antes de que el usuario
complete el proceso (etapa3). Al finalizar se borra. Si el registro no se completa,
el registro temporal queda en la BD (se puede limpiar con un job periódico en el futuro).

### 2. `RegistroTemporal` también para recuperar clave
Cuando un usuario pide recuperar su clave, se reutiliza `RegistroTemporal` para guardar
el token de recuperación. **Alternativa considerada**: tabla separada `TokenRecuperacion`.
Se descartó por simplicidad (proyecto académico). Si en el futuro se implementa el email,
reconsiderar.

### 3. `recuperarClave` siempre devuelve 200
Aunque el email no exista, el endpoint devuelve 200 con mensaje genérico.
**Motivo**: evitar enumeration attack (revelar si un email está registrado).
El servicio usa `ifPresent` para no tirar excepción.

### 4. `@Transactional` en `etapa3` y `cambiarClave`
Ambos hacen un `save` seguido de un `delete`. Sin `@Transactional`, si el `delete`
falla la BD queda inconsistente. Con la anotación, ambas operaciones son atómicas.

### 5. `fechaNacimiento` en `Usuario` pero no en DTOs
El campo existe en la entidad por si se necesita en el futuro (perfil, verificación de edad
para subastas). No se recolecta en el flujo de registro actual porque la docu no lo indica.

### 6. No se usa `UserDetailsService`
Spring Security generalmente requiere `UserDetailsService` para autenticar. En este proyecto
se omite porque el auth es 100% stateless con JWT. El `JwtFilter` setea el contexto de
seguridad directamente con el email extraído del token. No hay sesiones, no hay cookies.

### 7. Contraseñas con BCrypt
`PasswordEncoder` configurado como `BCryptPasswordEncoder` en `SecurityConfig`.
Se inyecta donde se necesita (AuthService, DataSeeder). No está hardcodeado en ningún lado.

---

## DataSeeder

Activo en todos los perfiles excepto `prod`.
Se salta si ya hay usuarios en la BD (idempotente).
Crea 3 usuarios con clave `password123`:
- `juan@quickbid.com`
- `maria@quickbid.com`
- `carlos@quickbid.com`

Para ver tokens de verificación durante desarrollo: abrir `http://localhost:8080/h2-console`
con JDBC URL `jdbc:h2:file:./quickbiddb`.

---

## Lo que está hecho

| Módulo | Estado |
|---|---|
| Infraestructura (pom.xml, application.properties) | ✅ |
| `ApiResponse<T>` | ✅ |
| `AppException` + `GlobalExceptionHandler` | ✅ |
| `JwtUtil`, `JwtFilter`, `SecurityConfig` | ✅ |
| `Usuario` entity + `UsuarioRepository` | ✅ |
| `RegistroTemporal` entity + Repository | ✅ |
| Auth module completo (8 endpoints) | ✅ |
| `DataSeeder` con 3 usuarios de prueba | ✅ |

---

## Orden de implementación acordado

| Prioridad | Módulo | Estado |
|---|---|---|
| 🔴 P0 | Auth (login + registro 3 etapas) | ✅ |
| 🔴 P0 | GET /api/subastas (lista) | ⏳ |
| 🟡 P1 | GET /api/subastas/{id} + catálogo + item | ⏳ |
| 🟡 P1 | Perfil + Notificaciones + Historial | ⏳ |
| 🟢 P2 | Métodos de Pago | ⏳ |
| 🟢 P2 | Inscripción + Puja | ⏳ |
| ⚪ P3 | Compras + Consignación | ⏳ |

---

## Repos

- **Frontend**: https://github.com/AgustinNari/FE-DAI (branch: setup/windows-build-fix)
- **Backend**: https://github.com/AgustinNari/-Backend-Desarrollo-de-Aplicaciones-I
- **Local backend**: `C:\Users\lusso\Desktop\FE-DAI\BackendQuickbid\quickbid`
