#!/usr/bin/env bash
# Compila el APK de la app TerraCode (Flet) dentro de Docker en el VPS y lo publica en
# https://<DOMINIO>/descargas/terracode.apk (lo sirve Caddy desde deploy/descargas).
#
#   bash deploy/apk/construir-apk.sh /opt/terracode-src.zip
#
# El zip es el código del frontend (D:\TerraCode sin .venv, build ni .git).
# La primera vez descarga la imagen con el SDK de Android y Flutter (~5 GB) y tarda 15-25 min;
# después reutiliza la caché (volumen terracode-apk-cache) y tarda unos minutos.
set -euo pipefail

ZIP=${1:?uso: bash deploy/apk/construir-apk.sh <zip del frontend>}
RAIZ=$(cd "$(dirname "$0")/../.." && pwd)
APP=/opt/terracode-app
DESCARGAS=$RAIZ/deploy/descargas
IMAGEN=ghcr.io/cirruslabs/flutter:stable

libre=$(df -BG --output=avail / | tail -1 | tr -dc 0-9)
if [ "$libre" -lt 12 ]; then
  echo "Hacen falta al menos 12 GB libres en disco; hay ${libre} GB." >&2
  exit 1
fi
command -v unzip >/dev/null || { apt-get update -qq && apt-get install -y -qq unzip; }

echo "[$(date +%T)] Preparando el código en $APP..."
rm -rf "$APP" && mkdir -p "$APP"
unzip -q "$ZIP" -d "$APP"

echo "[$(date +%T)] Compilando el APK (la primera vez tarda)..."
docker run --rm \
  -v "$APP":/app \
  -v terracode-apk-cache:/root \
  -w /app \
  "$IMAGEN" bash -c '
    set -e
    command -v uv >/dev/null || [ -x /root/.local/bin/uv ] || curl -LsSf https://astral.sh/uv/install.sh | sh
    export PATH=/root/.local/bin:$PATH
    # Flutter se descomprime con otro dueño (uid del archivo): sin esto git lo rechaza ("dubious ownership")
    git config --global --add safe.directory "*"
    uv run --group dev flet build apk --yes
  '

APK=$(ls -1 "$APP"/build/apk/*.apk 2>/dev/null | head -1 || true)
if [ -z "$APK" ]; then
  echo "No se generó ningún .apk en $APP/build/apk" >&2
  exit 1
fi
mkdir -p "$DESCARGAS"
cp "$APK" "$DESCARGAS/terracode.apk"
echo "[$(date +%T)] Listo: $(du -h "$DESCARGAS/terracode.apk" | cut -f1) en $DESCARGAS/terracode.apk"
echo "Descarga: https://\$DOMINIO/descargas/terracode.apk"
