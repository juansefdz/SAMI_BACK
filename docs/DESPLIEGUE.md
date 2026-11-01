# Despliegue de SAMI

| Dominio | Servicio | Repositorio | Imagen |
|---|---|---|---|
| `sami.com` | Landing comercial (`/es`, `/en`) | SAMI_FRONT | `docker build --build-arg APP=landing -t sami-landing .` |
| `app.sami.com` | Plataforma (cliente, despacho, tripulación) | SAMI_FRONT | `docker build --build-arg APP=platform -t sami-platform .` |
| `api.sami.com` | API REST + WebSocket (`/api/v1`) | SAMI_BACK | `docker build -t sami-api .` |
| Tiendas | App Android/iOS | SAMI_MOBILE | `eas build -p android|ios --profile production` |

## 1. Base de datos

MySQL 8 administrado (o el contenedor de `docker-compose.yml` solo para desarrollo). Flyway crea el esquema en el
primer arranque de la API. Respaldos diarios y acceso solo desde la red de la API.

## 2. API (`api.sami.com`)

Variables mínimas:

```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<host>:3306/sami?serverTimezone=UTC
DB_USER=...
DB_PASSWORD=...
JWT_SECRET=<openssl rand -base64 64>
CORS_ALLOWED_ORIGINS=https://app.sami.com,https://sami.com
GOOGLE_MAPS_API_KEY=...            # restringida por IP del servidor, solo Directions API
SAMI_ADMIN_EMAIL=...               # solo el primer despliegue
SAMI_ADMIN_PASSWORD=...
```

El proxy (Nginx, Traefik, balanceador de la nube) debe:

- Terminar TLS y reenviar `X-Forwarded-For` (lo usa el límite de solicitudes sin cuenta).
- Permitir **WebSocket** en `/api/v1/ws`:

```nginx
location /api/v1/ws {
    proxy_pass http://sami-api:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_read_timeout 75s;
}
location / {
    proxy_pass http://sami-api:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Con más de una instancia de la API: mover el broker STOMP a RabbitMQ y el límite por IP a Redis
(ambos están en memoria hoy).

## 3. Web (`sami.com` y `app.sami.com`)

Las variables `NEXT_PUBLIC_*` se fijan **al construir** la imagen:

```bash
# Landing
docker build --build-arg APP=landing \
  --build-arg NEXT_PUBLIC_SITE_URL=https://sami.com \
  --build-arg NEXT_PUBLIC_APP_URL=https://app.sami.com \
  -t sami-landing .

# Plataforma
docker build --build-arg APP=platform \
  --build-arg NEXT_PUBLIC_API_URL=https://api.sami.com/api/v1 \
  --build-arg NEXT_PUBLIC_SITE_URL=https://sami.com \
  --build-arg NEXT_PUBLIC_GOOGLE_MAPS_API_KEY=... \
  -t sami-platform .

docker run -p 3000:3000 sami-landing
docker run -p 3001:3000 sami-platform
```

Alternativa sin Docker: Vercel con dos proyectos apuntando a `apps/landing` y `apps/platform` (Root Directory) y la
raíz del monorepo como contexto de instalación.

La clave de Google Maps del navegador debe restringirse por referrer a `https://app.sami.com/*`.

## 4. App móvil

```bash
cd SAMI_MOBILE
EXPO_PUBLIC_API_URL=https://api.sami.com/api/v1 eas build -p android --profile production
EXPO_PUBLIC_API_URL=https://api.sami.com/api/v1 eas build -p ios --profile production
eas submit -p android && eas submit -p ios
```

Tras publicar, define `NEXT_PUBLIC_APP_STORE_URL` y `NEXT_PUBLIC_PLAY_STORE_URL` en la landing.

## 5. Antes de salir a producción

- [ ] Fotografía propia y con licencia (ver `SAMI_FRONT/docs/FOTOGRAFIA.md`).
- [ ] Cobertura real en `apps/landing/src/content/coverage.ts` y respaldo en `content/trust.ts`.
- [ ] `JPA_DDL_AUTO=validate` en un entorno de pruebas para confirmar que entidades y migraciones coinciden.
- [ ] Política de privacidad y tratamiento de datos (Ley 1581 de 2012): la solicitud sin cuenta guarda nombre, teléfono y ubicación.
- [ ] Monitoreo: `/api/v1/actuator/health` en el balanceador y alertas de errores 5xx.
