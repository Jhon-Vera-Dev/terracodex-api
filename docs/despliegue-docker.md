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
no necesita CORS; solo un build web (`flet build web`) necesita su origen en `CORS_ALLOWED_ORIGINS`.

## 6. Servicio de recomendación (servicio-ml)

`deploy/docker-compose.yml` levanta también `ml` (Python + `crop_model_v4.joblib`). Solo lo ve la API por la red
interna (`ML_URL=http://ml:8000`), con la clave `ML_API_KEY`. No publica puertos ni pasa por Caddy. Usa unos
400 MB de RAM. La primera recomendación de cada punto tarda hasta 1 minuto (NASA POWER) y luego queda en caché.

```bash
docker compose -f deploy/docker-compose.yml logs -f ml        # "Application startup complete"
docker compose -f deploy/docker-compose.yml exec ml python -c "import urllib.request;print(urllib.request.urlopen('http://127.0.0.1:8000/health').read())"
```

Para cambiar el modelo, reemplaza `servicio-ml/models/crop_model_v4.joblib` y reconstruye: `up -d --build ml`.

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
