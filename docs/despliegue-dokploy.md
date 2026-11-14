# Despliegue en Hostinger con Dokploy

API + PostgreSQL en un VPS de Hostinger administrado con Dokploy. Dokploy construye la imagen con el `Dockerfile`
del repo, la publica con HTTPS (Traefik + Let's Encrypt) y redespliega en cada push.

> La cuenta de Hostinger es compartida: crear, reinstalar o cambiar un VPS, el DNS o el firewall se hace solo con
> confirmación explícita del dueño. No instales Dokploy en el VPS de app-zonal (`srv1608280`): Dokploy toma los
> puertos 80/443, que ahí ya usa Caddy.

## 0. Antes de empezar

- `./mvnw test` en verde.
- El código en un repositorio Git (GitHub, GitLab o Gitea). Dokploy construye desde ahí.
- Un VPS con 2 GB de RAM como mínimo (4 GB recomendado): Dokploy + PostgreSQL + la JVM.
- Un dominio o subdominio para el API, por ejemplo `api.tudominio.com`.

## 1. VPS con Dokploy

1. En hPanel → VPS, elige la plantilla de sistema operativo con Dokploy si el catálogo la ofrece. Si no, instala
   Ubuntu 24.04 y como root ejecuta el instalador oficial:
   ```bash
   curl -sSL https://dokploy.com/install.sh | sh
   ```
2. Entra a `http://<IP-del-VPS>:3000` y crea la cuenta de administrador de Dokploy.
3. En el firewall del VPS deja abiertos solo 22, 80 y 443 (y 3000 mientras configuras; luego publica el panel con un
   dominio y ciérralo).

## 2. DNS

Crea un registro `A` de `api.tudominio.com` hacia la IP del VPS. Espera a que resuelva antes del paso 5, o el
certificado HTTPS falla.

## 3. PostgreSQL

Dokploy → Project `terracodex` → Create Service → Database → PostgreSQL 17.

- Database: `terracodex`, User: `terracodex`, Password: una larga y aleatoria.
- No la expongas a internet (sin "External Port").
- Copia el **Internal Host**: lo usa el API en `DB_URL`.
- Backups: configura un destino S3 (o compatible) y un respaldo diario.

## 4. Aplicación

Create Service → Application → `terracodex-api`.

- Provider: tu repositorio y la rama `main`.
- Build Type: **Dockerfile** (ruta `Dockerfile`).
- Environment: copia los nombres de `.env.ejemplo` con los valores reales. Genera el secreto con
  `openssl rand -base64 48`.
- Deploy. El primer arranque crea las tablas (Flyway) y el usuario `admin` con `ADMIN_PASSWORD`.

## 5. Dominio

En la aplicación → Domains: host `api.tudominio.com`, puerto del contenedor **8080**, HTTPS con Let's Encrypt.

Comprueba:

```bash
curl https://api.tudominio.com/actuator/health
curl -X POST https://api.tudominio.com/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"admin","contrasena":"<ADMIN_PASSWORD>","canal":"WEB"}'
```

## 6. Frontend (D:\TerraCode)

Ejecuta la app con `TERRACODE_API_URL=https://api.tudominio.com`. La app de escritorio o móvil llama al API
directamente y no necesita CORS; solo un build web (`flet build web`) necesita su origen en `CORS_ALLOWED_ORIGINS`.

## Actualizar y revertir

- Activa Auto Deploy (webhook) para redesplegar en cada push a `main`, o pulsa Deploy a mano.
- Para revertir, Deployments → redeploy de una versión anterior. Las migraciones Flyway no se deshacen: una
  migración nueva debe ser compatible con la versión anterior del código, o se corrige con otra migración.

## Seguridad

- `ADMIN_PASSWORD` solo se usa si la base no tiene usuarios. Después cambia la contraseña desde el API
  (`PUT /api/seguridad/mi-contrasena`).
- Cambiar `JWT_SECRET` cierra todas las sesiones.
- `/actuator/**` (salvo health e info) exige rol ADMIN.
