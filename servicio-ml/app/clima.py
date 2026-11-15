"""Variables climáticas históricas (1981-2010) de NASA POWER: las mismas que usó el entrenamiento del modelo.

Copia de ``historical_features`` de ``src/ml/build_dataset.py`` (rama develop), sin las dependencias de entrenamiento
(rasterio, pandas).
"""

from calendar import monthrange
from functools import lru_cache
from math import isfinite
from statistics import mean

import httpx

NASA_URL = "https://power.larc.nasa.gov/api/temporal/monthly/point"

METRICS = {
    "T2M": "temperature_mean",
    "T2M_MIN": "temperature_min_monthly_mean",
    "T2M_MAX": "temperature_max_monthly_mean",
    "PRECTOTCORR": "precipitation_annual_mm",
    "RH2M": "air_humidity",
    "GWETTOP": "surface_soil_wetness",
    "GWETROOT": "root_soil_wetness",
    "WS2M": "wind_speed",
}

_client = httpx.Client(timeout=httpx.Timeout(60, connect=10), transport=httpx.HTTPTransport(retries=1))


class DatosNoDisponibles(Exception):
    """NASA POWER no respondió o no tiene la serie completa para el punto."""


def _calcular(data: dict) -> dict:
    parameters = data["properties"]["parameter"]
    fill = data["header"]["fill_value"]
    features = {}
    for metric, name in METRICS.items():
        series = parameters.get(metric, {})
        values = []
        for year in range(1981, 2011):
            for month in range(1, 13):
                value = series.get(f"{year}{month:02d}")
                if value is None or value == fill or not isfinite(value):
                    raise DatosNoDisponibles(f"Datos incompletos de NASA POWER: {metric}")
                values.append(value * monthrange(year, month)[1] if metric == "PRECTOTCORR" else value)
        features[name] = sum(values) / 30 if metric == "PRECTOTCORR" else mean(values)
    return features


@lru_cache(maxsize=512)
def variables_historicas(lat: float, lon: float) -> dict:
    """Las coordenadas llegan redondeadas a 2 decimales: NASA POWER tiene celdas de 0.5°, así se reutiliza la caché."""
    try:
        response = _client.get(
            NASA_URL,
            params={
                "latitude": lat,
                "longitude": lon,
                "community": "AG",
                "parameters": ",".join(METRICS),
                "start": 1981,
                "end": 2010,
                "format": "JSON",
            },
        )
        response.raise_for_status()
        return _calcular(response.json())
    except (httpx.HTTPError, KeyError, ValueError) as e:
        raise DatosNoDisponibles("No se pudieron obtener los datos climáticos de NASA POWER") from e
