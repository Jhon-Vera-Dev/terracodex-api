# Versión web de la app TerraCode (Flet) servida por Python: cada navegador abre una sesión por WebSocket y la
# lógica corre en este contenedor. Caddy la publica en https://<DOMINIO>/app/.
#
# El código del frontend (D:\TerraCode) lo deja deploy/web/publicar-web.sh en deploy/web/app (no va al repo).
FROM python:3.12-slim

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    FLET_FORCE_WEB_SERVER=true \
    FLET_SERVER_IP=0.0.0.0 \
    FLET_SERVER_PORT=8551 \
    FLET_WEB_APP_PATH=app \
    FLET_ASSETS_DIR=/app/assets

# Mismas versiones que el pyproject.toml del frontend; flet-web trae el cliente web (incluye el GPS)
RUN pip install --no-cache-dir "flet[web]==1.1.2" "flet-geolocator==1.1.2" "certifi>=2024.8.30"

RUN useradd --system --uid 10001 --no-create-home terracode
WORKDIR /app
COPY --chown=terracode:terracode app/src/ ./
USER terracode

EXPOSE 8551
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8551/app/', timeout=4)"

CMD ["python", "main.py"]
