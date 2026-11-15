#!/usr/bin/env bash
# Publica la app TerraCode web (HTML/JS estático, servida por Caddy en https://<DOMINIO>/app/) y el explorador de
# TerraSprout (Flet, contenedor "explorador" en EXPLORADOR_DOMINIO).
#
#   bash deploy/web/publicar-web.sh /opt/terracode-src.zip
#
# El zip es el código del frontend (D:\TerraCode: src, web, models...), el mismo del APK.
# Reemplaza deploy/web/app, reconstruye la imagen del explorador y recarga Caddy.
set -euo pipefail

ZIP=${1:?uso: bash deploy/web/publicar-web.sh <zip del frontend>}
RAIZ=$(cd "$(dirname "$0")/../.." && pwd)
APP=$RAIZ/deploy/web/app
command -v unzip >/dev/null || { apt-get update -qq && apt-get install -y -qq unzip; }

echo "[$(date +%T)] Preparando el código en $APP..."
rm -rf "$APP" && mkdir -p "$APP"
unzip -q "$ZIP" -d "$APP"
if [ ! -f "$APP/src/main.py" ] || [ ! -f "$APP/models/crop_model_v4.joblib" ] || [ ! -f "$APP/web/index.html" ]; then
  echo "El zip no tiene src/main.py, web/index.html y models/crop_model_v4.joblib en la raíz: comprime el contenido de D:\TerraCode." >&2
  exit 1
fi

# Tarjeta "Explorador" del home de la app web: la URL sale de EXPLORADOR_DOMINIO del .env (vacío: no se muestra)
EXPLORADOR=$(grep -E '^EXPLORADOR_DOMINIO=' "$RAIZ/deploy/.env" 2>/dev/null | tail -1 | cut -d= -f2- | tr -d "\"' \r" || true)
if [ -n "$EXPLORADOR" ]; then
  case "$EXPLORADOR" in http*) ;; *) EXPLORADOR="https://$EXPLORADOR" ;; esac
  EXPLORADOR="${EXPLORADOR%/}/"
fi
printf 'window.TERRACODE_CONFIG = { explorador: "%s" };\n' "$EXPLORADOR" > "$APP/web/config.js"
# Caddy corre con otro usuario: los archivos deben poder leerse
chmod -R a+rX "$APP/web"

# La app web en Flet (contenedor "web") ya no se usa: si quedó de un despliegue anterior, se quita
docker ps -aq --filter "label=com.docker.compose.service=web" \
  --filter "label=com.docker.compose.project.working_dir=$RAIZ/deploy" | xargs -r docker rm -f >/dev/null

echo "[$(date +%T)] Construyendo y arrancando el explorador..."
docker compose -f "$RAIZ/deploy/docker-compose.yml" --profile web up -d --build explorador
# Primero valida el Caddyfile con la misma imagen: si tiene un error, para aquí y Caddy sigue con el anterior
docker compose -f "$RAIZ/deploy/docker-compose.yml" run --rm --no-deps caddy caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile
# Caddy toma el Caddyfile y la landing actualizados sin cortar el HTTPS (si cambió el docker-compose.yml, se recrea)
docker compose -f "$RAIZ/deploy/docker-compose.yml" up -d caddy
docker compose -f "$RAIZ/deploy/docker-compose.yml" exec caddy caddy reload --config /etc/caddy/Caddyfile

echo "[$(date +%T)] Listo. Landing: https://\$DOMINIO/   App: https://\$DOMINIO/app/"
