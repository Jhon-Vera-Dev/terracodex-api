# Despliegue en Hostinger con Docker Compose

PostgreSQL + API + Caddy en un VPS de Hostinger con Docker (`deploy/docker-compose.yml`). Solo Caddy publica los
puertos 80 y 443; con dominio, Caddy obtiene el certificado HTTPS solo.

> La cuenta de Hostinger es compartida: crear, reinstalar o cambiar un VPS, el DNS o el firewall se hace solo con
> confirmación explícita del dueño. Este VPS (76.13.225.20) es solo de terracodex; el de app-zonal es otro.

## 1. Primera vez

En la terminal del VPS (hPanel → VPS → Terminal, o `ssh root@76.13.225.20`):

```bash
docker --version && docker compose version     # deben responder

git clone https://github.com/Jhon-Vera-Dev/terracodex-api.git /opt/terracodex
cd /opt/terracodex
cp deploy/.env.ejemplo deploy/.env
openssl rand -base64 32      # → POSTGRES_PASSWORD
openssl rand -base64 48      # → JWT_SECRET
openssl rand -hex 32         # → ML_API_KEY (clave entre la API y servicio-ml)
nano deploy/.env             # completa POSTGRES_PASSWORD, JWT_SECRET, ML_API_KEY, ADMIN_PASSWORD (y DOMINIO si ya lo tienes)
chmod 600 deploy/.env

docker compose -f deploy/docker-compose.yml up -d --build
docker compose -f deploy/docker-compose.yml logs -f api   # espera "Started TerracodexApiApplication"
```

El repo es privado: `git clone` pide usuario (`Jhon-Vera-Dev`) y como contraseña un token de GitHub
(Settings → Developer settings → Personal access tokens, permiso de solo lectura del repo).

## 2. Firewall

hPanel → VPS → Firewall: permite solo 22, 80 y 443. PostgreSQL y la API no publican puertos.

## 3. Comprobar

```bash
curl http://76.13.225.20/actuator/health
curl -X POST http://76.13.225.20/api/auth/login -H 'Content-Type: application/json' \
  -d '{"login":"admin","contrasena":"<ADMIN_PASSWORD>","canal":"WEB"}'
```

Sin dominio todo va por HTTP: sirve para probar, pero las contraseñas viajan sin cifrar.

## 3b. Usuarios iniciales

Con el API arriba, desde el VPS (o desde tu PC cambiando `--api`):

```bash
cd /opt/terracodex
python3 scripts/seed/cargar_usuarios.py --api http://localhost
```

Pide la contraseña de `admin` y la contraseña inicial de los usuarios nuevos. Los usuarios y roles están en
`scripts/seed/usuarios.json` (sin contraseñas). Es idempotente: los que ya existen se saltan.

## 4. Dominio y HTTPS

1. DNS: registro `A` de `api.tudominio.com` → `76.13.225.20`.
2. En `deploy/.env`: `DOMINIO=api.tudominio.com`.
3. `docker compose -f deploy/docker-compose.yml up -d caddy`. Caddy pide el certificado solo.

## 5. Frontend (D:\TerraCode)

`TERRACODE_API_URL=http://76.13.225.20` (o `https://api.tudominio.com` con dominio). La app de escritorio o móvil
no necesita CORS; solo un build web estático (`flet build web`) necesitaría su origen en `CORS_ALLOWED_ORIGINS`; la versión web
publicada aquí (sección 8) corre en el servidor y no lo necesita.

## 6. Servicio de recomendación (servicio-ml)

`deploy/docker-compose.yml` levanta también `ml` (Python + `crop_model_v4.joblib`). Solo lo ve la API por la red
interna (`ML_URL=http://ml:8000`), con la clave `ML_API_KEY`. No publica puertos ni pasa por Caddy. Usa unos
400 MB de RAM. La primera recomendación de cada punto tarda hasta 1 minuto (NASA POWER) y luego queda en caché.

```bash
docker compose -f deploy/docker-compose.yml logs -f ml        # "Application startup complete"
docker compose -f deploy/docker-compose.yml exec ml python -c "import urllib.request;print(urllib.request.urlopen('http://127.0.0.1:8000/health').read())"
```

Para cambiar el modelo, reemplaza `servicio-ml/models/crop_model_v4.joblib` y reconstruye: `up -d --build ml`.

## 7. Login con Google

Cualquier cuenta de Google con correo verificado puede entrar. La primera vez se crea un usuario OPERADOR con
acceso solo móvil (login sacado del correo, por ejemplo `ana.flores`). El administrador puede cambiarle roles y
canales después. La contraseña del usuario es aleatoria y nadie la conoce: entra solo con Google, hasta que el
admin le ponga una.

1. [Google Cloud Console](https://console.cloud.google.com/) → crea un proyecto (o usa uno).
2. APIs y servicios → **Pantalla de consentimiento de OAuth**: tipo *Externo*, nombre «TerraCode», correo de soporte,
   alcances `openid`, `email` y `profile`. Publica la app (si queda en *Prueba*, solo entran los usuarios de prueba).
3. APIs y servicios → **Credenciales → Crear credenciales → ID de cliente de OAuth → Aplicación web**.
   - URI de redireccionamiento autorizados: `https://<DOMINIO>/api/auth/google/callback`
4. Copia el ID y el secreto del cliente en `deploy/.env`:
   ```
   GOOGLE_CLIENT_ID=xxxx.apps.googleusercontent.com
   GOOGLE_CLIENT_SECRET=GOCSPX-...
   GOOGLE_REDIRECT_URI=https://<DOMINIO>/api/auth/google/callback
   ```
5. `docker compose -f deploy/docker-compose.yml up -d api`

No hace falta un cliente Android ni la huella SHA-1 del APK: la app abre Google en el navegador y el backend hace el
canje (flujo de código de autorización). Sin estas variables, «Continuar con Google» responde «no está configurado».

## 8. Landing y versión web (Flet)

- `https://<DOMINIO>/`: landing estática (`deploy/landing`, copia adaptada de la de TerraSprout). Caddy la sirve
  directo desde la carpeta, sin contenedor. Sus botones llevan a la app (`/app/`) y al APK (`/descargas/`).
- `https://<DOMINIO>/app/`: la app de D:\TerraCode en el navegador. El contenedor `web` la ejecuta en Python (Flet
  en modo servidor) y cada navegador se conecta por WebSocket. Habla con la API por la red interna
  (`http://api:8080`), así que no necesita CORS.

Caddy lee su configuración de la carpeta `deploy/caddy`, montada entera: así ve el `Caddyfile` nuevo después de un
`git pull` o de descomprimir el código encima. Para aplicar cambios del Caddyfile o de la landing:
`docker compose -f deploy/docker-compose.yml exec caddy caddy reload --config /etc/caddy/Caddyfile`.

La app web usa el mismo zip del APK (contenido de D:\TerraCode sin `.venv`, `build` ni `.git`):

```bash
bash deploy/web/publicar-web.sh /opt/terracode-src.zip
docker compose -f deploy/docker-compose.yml --profile web logs -f web
```

- El servicio tiene el perfil `web`: un `up -d --build` sin `--profile web` no lo toca. Para actualizarlo, vuelve a
  correr el script con el zip nuevo.
- El GPS del navegador (Clima, Nuevo registro) solo funciona con HTTPS, o sea con `DOMINIO` configurado.
- Cada pestaña abierta es una sesión en el servidor (unos pocos MB de RAM); el límite del contenedor es 512 MB.
- Los usuarios creados con Google tienen solo acceso móvil: en la web el login con Google les responde que no tienen
  acceso web hasta que el administrador les active `isWeb`.

## Actualizar

```bash
cd /opt/terracodex
git pull
docker compose -f deploy/docker-compose.yml up -d --build api ml
```

Las migraciones Flyway se aplican al arrancar y no se deshacen: para revertir código, una migración nueva debe ser
compatible con la versión anterior, o se corrige con otra migración.

## Respaldos

```bash
docker compose -f deploy/docker-compose.yml exec -T postgres \
  pg_dump -U terracodex terracodex | gzip > /root/terracodex-$(date +%F).sql.gz
```

Prográmalo con `crontab -e` (por ejemplo, todos los días a las 3:00) y copia los archivos fuera del VPS.

## Seguridad

- `ADMIN_PASSWORD` solo se usa si la base no tiene usuarios. Después cambia la contraseña con
  `PUT /api/seguridad/mi-contrasena`.
- Cambiar `JWT_SECRET` cierra todas las sesiones.
- Caddy solo publica `/api/*` y `/actuator/health`; el resto del actuator no sale a internet.
