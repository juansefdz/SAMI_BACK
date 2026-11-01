<p align="center">
  <img src="https://i.imgur.com/rtTtkyj.png" alt="LOGO SAMI" width="220">
</p>

# SAMI API — Servicio Ambulatorio Médico Inmediato

API REST para solicitar, despachar y seguir ambulancias en tiempo real.
Spring Boot 3.3 · Java 17 · MySQL · JWT · MapStruct · Google Maps.

Clientes que la consumen: **SAMI_FRONT** (Next.js: landing comercial + plataforma) y **SAMI_MOBILE** (Expo, terminal Android/iOS).

## Puesta en marcha

```bash
cp .env.example .env              # y completar valores
docker compose up -d mysql        # MySQL 8 local (usuario/clave: sami/sami)
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run            # perfil dev por defecto
```

- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/api/v1/docs`
- Health: `http://localhost:8080/api/v1/actuator/health`

Para crear el primer administrador define `SAMI_ADMIN_EMAIL` y `SAMI_ADMIN_PASSWORD` antes de arrancar
(solo se crea si no existe ningún ADMIN).

Tests: `./mvnw verify` (usan H2 en memoria, no necesitan MySQL).

**Base de datos:** el esquema lo crean las migraciones de Flyway (`src/main/resources/db/migration`). Hibernate no
modifica tablas (`JPA_DDL_AUTO=none`); tras el primer arranque puedes usar `JPA_DDL_AUTO=validate` para comprobar que las
entidades coinciden. Si ya tenías tablas creadas por la versión anterior, usa una base nueva o `FLYWAY_BASELINE=true`.

**Docker:** `docker build -t sami-api .` y `docker run -p 8080:8080 --env-file .env sami-api`.

### Variables de entorno

| Variable | Descripción | Por defecto |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` o `prod` | `dev` |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Conexión MySQL | localhost / root / vacío |
| `JWT_SECRET` | Clave HMAC Base64 ≥ 256 bits (`openssl rand -base64 64`). **Obligatoria fuera de dev** | solo en dev |
| `JWT_EXPIRATION` | Duración ISO-8601 | `PT24H` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (plataforma web y Expo web), separados por coma | `http://localhost:3001,http://localhost:3000,http://localhost:8081` |
| `GOOGLE_MAPS_API_KEY` | Habilita tiempos estimados (Directions API). Sin ella la API funciona igual | vacío |
| `JPA_DDL_AUTO` | `none` (Flyway gestiona el esquema) o `validate` | `none` |
| `SAMI_ADMIN_EMAIL` / `SAMI_ADMIN_PASSWORD` | Administrador inicial (solo si no existe ninguno) | vacío |

## Estructura (paquete por funcionalidad)

```
src/main/java/com/sami/app
├── SamiApplication.java
├── common/            # transversal
│   ├── config/        # SamiProperties (sami.*), OpenAPI, admin inicial
│   ├── exception/     # ApiError + GlobalExceptionHandler + excepciones de negocio
│   └── web/           # PageResponse
├── security/          # SecurityConfig, JWT (servicio + filtro), handlers 401/403, Roles
├── account/           # Account (credenciales, UserDetails), Role
├── auth/              # login / registro
├── user/              # perfiles, personal (staff)
├── ambulance/         # flota, tripulación, ubicación GPS
├── hospital/          # hospitales destino
├── emergency/         # solicitudes de servicio y su ciclo de vida
├── map/               # seguimiento y rutas (Google Directions, con caché)
└── realtime/          # WebSocket/STOMP: eventos tras commit y autorización de suscripciones
```

Cada módulo contiene `Entidad`, `Repository`, `Service`, `Controller`, `Mapper` (MapStruct) y `dto/` (records).

## Roles

| Rol | Puede |
|---|---|
| `CLIENT` | Registrarse, pedir ambulancia, ver/cancelar sus emergencias, seguirlas en el mapa |
| `ADMIN` | Despachar (asignar ambulancia + hospital), gestionar flota, hospitales y personal |
| `DRIVER` / `EMT` | Ver su ambulancia y emergencia actual, reportar GPS, iniciar/completar servicio. `EMT` registra la anamnesis |

## Endpoints

Todas las rutas cuelgan de `/api/v1`. Las listas aceptan `?page=1&size=10&sort=campo,desc` y responden
`{ content, page, size, totalElements, totalPages }` (`page` empieza en 1).

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/auth/register` | público | Registro de cliente → token |
| POST | `/auth/login` | público | Login → token |
| POST | `/auth/guest-emergency` | público (3 por IP / 10 min) | Pide ambulancia **sin cuenta**: crea una cuenta de invitado, la emergencia y devuelve token para seguirla |
| POST | `/users/me/claim` | invitado | Asigna email y contraseña a la cuenta de invitado (devuelve token nuevo) |
| GET / PUT | `/users/me` | autenticado | Perfil propio |
| GET / POST | `/users` | ADMIN | Listar (`?role=`) / crear personal |
| GET / PUT / DELETE | `/users/{id}` | ADMIN | |
| GET | `/hospitals`, `/hospitals/{id}` | autenticado | |
| POST / PUT / DELETE | `/hospitals[/{id}]` | ADMIN | |
| GET | `/ambulances?status=` | ADMIN | |
| GET | `/ambulances/me` | DRIVER, EMT | Ambulancia asignada |
| PATCH | `/ambulances/{id}/location` | DRIVER, EMT | Reporte GPS de la terminal |
| POST / PUT / DELETE | `/ambulances[/{id}]` | ADMIN | |
| POST | `/emergencies` | CLIENT | Solicitar ambulancia |
| GET | `/emergencies?status=` | ADMIN | Tablero de despacho |
| GET | `/emergencies/me` | CLIENT | Historial propio |
| GET | `/emergencies/current` | DRIVER, EMT | Emergencia activa (204 si no hay) |
| GET | `/emergencies/{id}` | dueño, tripulación, ADMIN | |
| PATCH | `/emergencies/{id}/assignment` | ADMIN | `{ ambulanceId, hospitalId }` |
| PATCH | `/emergencies/{id}/status` | según rol | `{ status }` |
| PATCH | `/emergencies/{id}/anamnesis` | EMT, ADMIN | `{ anamnesis }` |
| GET | `/emergencies/{id}/tracking` | dueño, tripulación, ADMIN | Posiciones + ETA |
| DELETE | `/emergencies/{id}` | ADMIN | |

### Tiempo real (WebSocket / STOMP)

Endpoint `ws://<host>/api/v1/ws`. En el frame `CONNECT` se envía `Authorization: Bearer <jwt>`.

| Tópico | Quién puede suscribirse | Mensaje |
|---|---|---|
| `/topic/emergencies/{id}` | dueño, tripulación asignada, ADMIN | `{ emergencyId, type }` con `type` = `CREATED`, `STATUS`, `LOCATION`, `UPDATED`, `DELETED` |
| `/topic/dispatch` | ADMIN | los mismos eventos de todas las emergencias |

Los mensajes son avisos: el cliente vuelve a pedir `/emergencies/{id}` o `/tracking`. Se envían solo después de confirmar
la transacción. Los tiempos estimados de Google se guardan 30 s en memoria para no repetir consultas.

### Ciclo de vida de una emergencia

```
PENDING ──(ADMIN asigna)──► ASSIGNED ──(tripulación)──► IN_PROGRESS ──(tripulación)──► COMPLETED
   └────────────(CLIENT o ADMIN cancela)───┴──► CANCELED
```
Al asignar, la ambulancia pasa a `BUSY`; al completar o cancelar vuelve a `AVAILABLE`.

### Errores

Todas las respuestas de error usan el mismo formato:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "message": "La solicitud tiene campos inválidos", "path": "/api/v1/emergencies",
  "fieldErrors": [{ "field": "severity", "message": "debe ser menor que o igual a 5" }] }
```

## Equipo

Juan Sebastián Fernández Montoya · Alejandro Quiceno Pérez · Ana María Restrepo Quintero · Jhonatan Toro Hurtado

Diagramas: [navegación](https://i.imgur.com/YjcCdhF.png) · [UML](https://i.imgur.com/SdTH2TU.png) · [arquitectura](https://i.imgur.com/55TMojh.png)
