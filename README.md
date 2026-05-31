# QuickBid — Backend

API REST Spring Boot para la app de subastas QuickBid (TPO DAI - UADE).

---

## Requisitos

| Herramienta | Versión mínima | Verificar |
|---|---|---|
| Java JDK | 17 | `java -version` |
| Maven | 3.9 (o usar el wrapper) | `./mvnw -v` |
| Node / npm | 18+ | `node -v` |
| React Native CLI | latest | `npx react-native -v` |
| Android SDK | API 33+ | variable `ANDROID_HOME` |
| ADB | cualquiera | `adb version` |

> El proyecto incluye Maven Wrapper (`mvnw.cmd`). No necesitás Maven instalado globalmente.

---

## Estructura del monorepo

```
FE-DAI/
├── BackendQuickbid/
│   └── quickbid/          ← proyecto Spring Boot (este repo)
├── FrontendQuickbid/      ← app React Native
└── dev-start.ps1          ← script de arranque unificado
```

---

## Levantar solo el backend

```powershell
cd BackendQuickbid\quickbid
.\mvnw.cmd spring-boot:run
```

El backend queda en `http://localhost:8080`.  
La base de datos H2 se guarda en `quickbid/quickbiddb.mv.db` (persiste entre reinicios).

### H2 Console (explorar la base de datos)

Abrir en el navegador: `http://localhost:8080/h2-console`

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./quickbiddb` |
| User | `sa` |
| Password | *(vacío)* |

---

## Levantar todo con el script (recomendado)

Desde la raíz del monorepo (`FE-DAI/`):

```powershell
.\dev-start.ps1
```

El script hace automáticamente:
1. Mata procesos que usen los puertos 8080 y 8081
2. Crea regla de firewall para el puerto 8080 (primera vez)
3. Levanta el backend y espera que esté listo
4. Detecta la IP local y actualiza `FrontendQuickbid/src/api/config.ts`
5. Configura `adb reverse` para que el celular use `localhost`
6. Lanza la app en el dispositivo conectado
7. Abre Metro bundler en una ventana separada

### Flags opcionales

```powershell
.\dev-start.ps1 -SkipDevice   # No conecta el celular por ADB
.\dev-start.ps1 -WifiOnly     # Usa IP WiFi en lugar de ADB reverse
```

> **Primera vez:** puede aparecer un prompt de UAC para crear la regla de firewall. Aceptar.

### Detección de ADB

El script busca ADB automáticamente en:
- `%LOCALAPPDATA%\Android\Sdk\platform-tools\` (Android Studio default)
- `C:\`, `D:\`, `E:\Android\Sdk\platform-tools\`
- El PATH del sistema

Si no lo encuentra, mostará un aviso con las opciones. La alternativa más simple es agregar `platform-tools` al PATH de Windows.

---

## Conectar el celular (Android físico)

1. Activar **Opciones de desarrollador** en el celu
2. Habilitar **Depuración USB**
3. Conectar por USB y aceptar el prompt de autorización
4. Verificar con `adb devices` — debe aparecer el dispositivo

El script corre `adb reverse tcp:8080 tcp:8080` y `tcp:8081 tcp:8081` para que el celu acceda al backend y Metro usando `localhost`.

---

## Usuarios de prueba

El servidor carga datos de prueba automáticamente al iniciar (perfil dev):

| Email | Contraseña |
|---|---|
| juan@quickbid.com | password123 |
| maria@quickbid.com | password123 |
| carlos@quickbid.com | password123 |

---

## Tokens de verificación (dev)

El backend no envía emails — los tokens aparecen en el log de consola con el prefijo `[DEV]`.

Ver tokens en tiempo real:

```powershell
Get-Content $env:TEMP\quickbid-backend.log -Wait | Select-String '\[DEV\]'
```

O en la consola donde corre el backend, buscar líneas como:

```
[DEV] Token de verificación para test@example.com: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

---

## Endpoints — módulo Auth

Base URL: `http://localhost:8080/api/auth`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `/registro/etapa1` | Datos personales | No |
| POST | `/registro/etapa2` | Foto DNI + genera token | No |
| POST | `/registro/verificar-token` | Verificar token email | No |
| POST | `/registro/etapa3` | Crear clave y finalizar registro | No |
| POST | `/registro/reenviar-link` | Reenviar token de verificación | No |
| POST | `/login` | Autenticarse y obtener JWT | No |
| POST | `/recuperar-clave` | Iniciar recuperación (siempre 200) | No |
| PUT | `/cambiar-clave` | Cambiar clave con token | No |

Todos los demás módulos requieren `Authorization: Bearer <token>` en el header.

### Ejemplo: login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@quickbid.com","clave":"password123"}' | jq .
```

Respuesta:
```json
{
  "data": {
    "token": "eyJ...",
    "email": "juan@quickbid.com",
    "nombre": "Juan",
    "apellido": "Perez"
  },
  "message": "Login exitoso"
}
```

---

## Formato estándar de respuesta

```json
{
  "data": { ... },
  "message": "Descripción del resultado",
  "errors": ["solo presente si hay errores de validación"]
}
```

---

## Variables de entorno / configuración

El archivo principal es `src/main/resources/application.properties`:

```properties
# Base de datos H2 (archivo, persiste entre reinicios)
spring.datasource.url=jdbc:h2:file:./quickbiddb

# JWT — cambiar en producción
jwt.secret=<clave-larga-base64>
jwt.expiration-ms=86400000

# DevTools
spring.devtools.restart.enabled=true
```

---

## Troubleshooting

**Puerto 8080 ocupado**
```powershell
(Get-NetTCPConnection -LocalPort 8080).OwningProcess | % { Stop-Process -Id $_ -Force }
```

**El celu no aparece en `adb devices`**
- Verificar que USB Debugging esté habilitado
- Cambiar el modo USB a "Transferencia de archivos" (MTP) — algunos celulares no responden en modo "Solo carga"
- Revocar autorizaciones ADB en Opciones de desarrollador y reconectar

**Metro no conecta con el celu**
```powershell
adb reverse tcp:8081 tcp:8081
adb reverse tcp:8080 tcp:8080
adb reverse --list   # verificar
```

**La app carga el bundle anterior (no el de Metro)**
```powershell
adb shell am force-stop com.frontendquickbid
adb shell am start -n com.frontendquickbid/.MainActivity
```

**Error de compilación Java**
```powershell
.\mvnw.cmd clean spring-boot:run
```
