# Versión web de la app TerraCode (Flet) servida por Python: cada navegador abre una sesión por WebSocket y la
# lógica corre en este contenedor. Caddy la publica en https://<DOMINIO>/app/.
#
# La misma imagen corre el explorador de TerraSprout (src/explorador.py) en el servicio "explorador".
# El código del frontend (D:\TerraCode) lo deja deploy/web/publicar-web.sh en deploy/web/app (no va al repo).
FROM python:3.12-slim

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    FLET_FORCE_WEB_SERVER=true \
    FLET_SERVER_IP=0.0.0.0 \
    FLET_SERVER_PORT=8551 \
    FLET_WEB_APP_PATH=app \
    FLET_ASSETS_DIR=/srv/terracode/src/assets

# Mismas versiones que el pyproject.toml del frontend ([project] + grupo "explorador"); flet-web trae el cliente
# web (incluye GPS, mapa y WebView)
RUN pip install --no-cache-dir "flet[web]==1.1.2" "flet-geolocator==1.1.2" "certifi>=2024.8.30" \
    "flet-map==1.1.2" "flet-webview==1.1.2" "Pillow>=11.3" "python-dotenv>=1,<2" "httpx>=0.28.1,<1" \
    "joblib==1.6.0" "numpy==2.2.6" "pandas==2.3.3" "rasterio==1.4.4" "scikit-learn==1.7.2" "scipy==1.15.3" \
    "reportlab>=4,<5"

RUN useradd --system --uid 10001 --no-create-home terracode
WORKDIR /srv/terracode/src
COPY --chown=terracode:terracode app/src/ ./
# Modelo del explorador (src/ml/predictor.py lo busca en ../models)
COPY --chown=terracode:terracode app/models/crop_model_v4.joblib /srv/terracode/models/crop_model_v4.joblib
USER terracode

EXPOSE 8551
# FLET_WEB_APP_PATH: "app" para TerraCode, vacío para el explorador (en la raíz de su subdominio)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD python -c "import os, urllib.request; p = os.getenv('FLET_WEB_APP_PATH', '').strip('/'); urllib.request.urlopen('http://127.0.0.1:8551/' + (p + '/' if p else ''), timeout=4)"

CMD ["python", "main.py"]
