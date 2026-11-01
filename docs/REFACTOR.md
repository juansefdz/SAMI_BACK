# Refactor 2026 — qué cambió y por qué

## Fallos corregidos

| # | Problema anterior | Corrección |
|---|---|---|
| 1 | `GoogleMapConfig` construía `GeoApiContext` con API key `null` (sin `@Value`), impidiendo arrancar | Bean condicional: solo existe si hay `GOOGLE_MAPS_API_KEY`; sin ella las ETA se omiten |
| 2 | En `SecurityConfig` el orden de reglas hacía que DRIVER/EMT/CLIENT nunca accedieran a `/ambulance` y `/service`; `/hospital` sin control de rol; `/locations/**` no coincidía con la ruta real | Autorización declarativa con `@PreAuthorize` por endpoint + `@EnableMethodSecurity` |
| 3 | Secreto JWT y credenciales de BD en el código | Variables de entorno, validadas al arrancar (`SamiProperties`) |
| 4 | `update()` creaba una entidad nueva: se perdía `account` del usuario y `price` del servicio | Se modifica la entidad existente con `@MappingTarget` |
| 5 | `/auth/register` creaba cuenta sin perfil de usuario | Registro crea cuenta + perfil en una transacción |
| 6 | "No encontrado" y token vencido devolvían 500 | `ResourceNotFoundException` → 404; filtro JWT tolerante → 401 JSON |
| 7 | `@Data` en relación bidireccional `Account`↔`User` (riesgo de StackOverflow en `toString`/`hashCode`) | `@Getter/@Setter`, relación unidireccional |
| 8 | Se podía asignar cualquier usuario como conductor/paramédico | Validación de rol y de que no esté en otra ambulancia |
| 9 | El cliente enviaba su propio `idClient` (suplantable) | El cliente sale del JWT |
| 10 | Clase `util/exceptions/Exception` que ocultaba `java.lang.Exception` | Eliminada |
| 11 | Filtro JWT anotado `@Component` (se registra dos veces) | Se instancia solo dentro de la cadena de seguridad |
| 12 | `@CrossOrigin("*")` en cada controller | CORS central configurable |

## Cambios de diseño (rompen compatibilidad)

- Paquete `com.sami.sami_app` → `com.sami.app`, organizado por funcionalidad.
- Rutas REST en plural sin verbos (`/ambulance/create` → `POST /ambulances`), `201 Created` y `204 No Content`.
- `ServiceEntity` → `Emergency` (`/emergencies`), con estados `PENDING → ASSIGNED → IN_PROGRESS → COMPLETED | CANCELED`.
  Se añadieron `description`, `patientsCount`, `severity`, `address` y marcas de tiempo. Se quitó `price` (no se usaba).
- `MapController /map/locations/{id}` → `GET /emergencies/{id}/tracking` con ETA ambulancia→paciente y →hospital.
- Nuevos: `PATCH /ambulances/{id}/location` (GPS desde la terminal móvil), `GET /ambulances/me`, `GET /emergencies/current`.
- Tablas renombradas a plural (`users`, `accounts`, `ambulances`, `hospitals`, `emergencies`). Con `ddl-auto=update` en dev se crean
  solas; los datos de las tablas viejas no se migran.
- Enums legibles: `RhType.AP` → `A_POSITIVE`, `ALS/BLS` → `ADVANCED/BASIC`, `ComplexityGrade` → `ComplexityLevel.LEVEL_I..IV`.
- DTOs como `record`, mapeo con MapStruct (antes se copiaba lo mismo en tres servicios con `BeanUtils`).
- Servicios concretos sin interfaces `I*` (no aportaban: una sola implementación).
- Paginación: `page` empieza en 1, respuesta `PageResponse` estable.

## Segunda iteración

- Solicitud **sin cuenta** (`POST /auth/guest-emergency`) con límite por IP y conversión posterior (`POST /users/me/claim`).
- **Flyway** gestiona el esquema (`V1__initial_schema.sql`); enums como `VARCHAR` y UUID como `CHAR(36)` explícitos.
- **WebSocket/STOMP** con eventos publicados tras commit; los clientes mantienen polling lento como respaldo.
- **Caché de 30 s** para las rutas de Google Directions.
- Dockerfile multi-etapa y CI en GitHub Actions.

## Siguientes pasos sugeridos

- Tests de integración por controller con `@WebMvcTest` + `spring-security-test`.
- Limitador y broker externos (Redis / RabbitMQ) si la API corre en varias instancias.
