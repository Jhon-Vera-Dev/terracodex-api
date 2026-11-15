#!/usr/bin/env bash
# Publica la versión web de TerraCode (Flet) en https://<DOMINIO>/app/
#
#   bash deploy/web/publicar-web.sh /opt/terracode-src.zip
#
# El zip es el código del frontend (D:\TerraCode sin .venv, build ni .git), el mismo del APK.
# Reemplaza deploy/web/app, reconstruye la imagen y reinicia solo el contenedor web (las sesiones abiertas se
# reconectan solas).
set -euo pipefail

ZIP=${1:?uso: bash deploy/web/publicar-web.sh <zip del frontend>}
RAIZ=$(cd "$(dirname "$0")/../.." && pwd)
APP=$RAIZ/deploy/web/app
command -v unzip >/dev/null || { apt-get update -qq && apt-get install -y -qq unzip; }

echo "[$(date +%T)] Preparando el código en $APP..."
rm -rf "$APP" && mkdir -p "$APP"
unzip -q "$ZIP" -d "$APP"
if [ ! -f "$APP/src/main.py" ]; then
  echo "El zip no tiene src/main.py en la raíz: comprime el contenido de D:\TerraCode, no la carpeta." >&2
  exit 1
fi

echo "[$(date +%T)] Construyendo y arrancando el contenedor web..."
docker compose -f "$RAIZ/deploy/docker-compose.yml" --profile web up -d --build web
# Primero valida el Caddyfile con la misma imagen: si tiene un error, para aquí y Caddy sigue con el anterior
docker compose -f "$RAIZ/deploy/docker-compose.yml" run --rm --no-deps caddy caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile
# Caddy toma el Caddyfile y la landing actualizados sin cortar el HTTPS (si cambió el docker-compose.yml, se recrea)
docker compose -f "$RAIZ/deploy/docker-compose.yml" up -d caddy
docker compose -f "$RAIZ/deploy/docker-compose.yml" exec caddy caddy reload --config /etc/caddy/Caddyfile

echo "[$(date +%T)] Listo. Landing: https://\$DOMINIO/   App: https://\$DOMINIO/app/"
