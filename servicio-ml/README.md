# servicio-ml

Recomendación de cultivos con `models/crop_model_v4.joblib` (scikit-learn), el modelo de la rama `develop`
(`src/ml/train_v4.py`). Lo consume solo `terracodex-api` por la red interna de Docker. No está expuesto a internet.

```
POST /recomendar  {"latitud": -14.06, "longitud": -75.73, "tipo_riego": "rainfed" | "irrigated_gravity" | "irrigated_drip"}
GET  /health
```

Respuesta: `recomendaciones` (código, cultivo y puntaje de aptitud de 0 a 100, de mayor a menor), `zona` (distrito,
provincia y departamento según OpenStreetMap), `periodo_referencia` y `error_medio` del modelo en ese escenario.

- **Clima**: NASA POWER, promedios mensuales de 1981 a 2010, las mismas variables del entrenamiento. La primera consulta
  de un punto tarda entre 5 y 60 s. Después queda en caché en memoria por celda de 0.01° (~1 km).
- **Zona**: Nominatim (OpenStreetMap). Admite como máximo 1 petición por segundo. Si no responde, la zona llega vacía y la
  recomendación igual se devuelve.
- **Seguridad**: si `ML_API_KEY` está definida, `/recomendar` exige el header `X-Api-Key`. La API la envía sola.

## Modelo

`models/crop_model_v4.joblib` (~42 MB) es una copia de `models/` de la rama `develop`. Si se reentrena, copia el
nuevo archivo aquí y reconstruye la imagen. Las versiones de `requirements.txt` deben ser las mismas del entrenamiento,
sobre todo scikit-learn.

## Local

```powershell
py -m venv .venv
.venv\Scripts\pip install -r requirements.txt pytest
.venv\Scripts\python -m pytest            # pruebas sin red (NASA y Nominatim simulados)
.venv\Scripts\python -m uvicorn app.main:app --port 8000
```

Con la API en local (`./mvnw spring-boot:run`) ya apunta a `http://localhost:8000` (`app.ml.url`).
