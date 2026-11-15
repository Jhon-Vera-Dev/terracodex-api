"""Recomendación de cultivos con ``crop_model_v4.joblib`` (mismo cálculo que ``src/ml/predictor.py``, rama develop)."""

import os
from functools import lru_cache
from pathlib import Path

import joblib
import pandas as pd

from app import clima

MODEL_PATH = Path(os.getenv("MODEL_PATH", Path(__file__).resolve().parents[1] / "models" / "crop_model_v4.joblib"))

TIPOS_RIEGO = ("rainfed", "irrigated_gravity", "irrigated_drip")


@lru_cache(maxsize=1)
def artefacto() -> dict:
    return joblib.load(MODEL_PATH)


def recomendar(lat: float, lon: float, tipo_riego: str) -> dict:
    art = artefacto()
    permitidos = art["crops_by_water_supply"].get(tipo_riego)
    if not permitidos:
        raise ValueError(f"Tipo de riego no disponible: {tipo_riego}")
    cultivos = {code: name for code, name in art["crops"].items() if code in permitidos}

    features = clima.variables_historicas(round(lat, 2), round(lon, 2))
    X = pd.DataFrame([{**features, "crop_code": code, "water_supply": tipo_riego} for code in cultivos])
    puntajes = art["model"].predict(X[art["features"]])

    recomendaciones = sorted(
        (
            {"codigo": code, "cultivo": name, "puntaje": round(min(max(float(score), 0.0), 100.0), 2)}
            for (code, name), score in zip(cultivos.items(), puntajes)
        ),
        key=lambda r: r["puntaje"],
        reverse=True,
    )
    return {
        "tipo_riego": tipo_riego,
        "periodo_referencia": art["reference_period"],
        "error_medio": round(float(art["metrics"]["scenario_mae"].get(tipo_riego, art["metrics"]["mae"])), 2),
        "recomendaciones": recomendaciones,
    }
