"""Servicio de recomendación de cultivos. Lo consume solo terracodex-api (red interna de Docker).

    POST /recomendar  {"latitud": -14.06, "longitud": -75.73, "tipo_riego": "rainfed"}
    GET  /health

Si se define ML_API_KEY, toda petición a /recomendar debe enviar el header ``X-Api-Key`` con ese valor.
"""

import hmac
import os
from contextlib import asynccontextmanager
from typing import Literal

from fastapi import Depends, FastAPI, Header, HTTPException
from fastapi.concurrency import run_in_threadpool
from pydantic import BaseModel, Field

from app import modelo, zona
from app.clima import DatosNoDisponibles

API_KEY = os.getenv("ML_API_KEY", "")


@asynccontextmanager
async def lifespan(app: FastAPI):
    modelo.artefacto()  # carga el modelo al arrancar, no en la primera petición
    yield


app = FastAPI(title="Terracodex servicio ML", lifespan=lifespan)


def verificar_clave(x_api_key: str | None = Header(default=None)):
    if API_KEY and not hmac.compare_digest(x_api_key or "", API_KEY):
        raise HTTPException(status_code=401, detail="Clave de servicio inválida")


class RecomendarIn(BaseModel):
    latitud: float = Field(ge=-90, le=90)
    longitud: float = Field(ge=-180, le=180)
    tipo_riego: Literal["rainfed", "irrigated_gravity", "irrigated_drip"] = "rainfed"


@app.get("/health")
def health():
    return {"status": "ok", "modelo": modelo.artefacto().get("version")}


@app.post("/recomendar", dependencies=[Depends(verificar_clave)])
async def recomendar(data: RecomendarIn):
    lat, lon = data.latitud, data.longitud
    try:
        resultado = await run_in_threadpool(modelo.recomendar, lat, lon, data.tipo_riego)
    except DatosNoDisponibles as e:
        raise HTTPException(status_code=503, detail=str(e)) from e
    resultado["zona"] = await run_in_threadpool(zona.buscar_zona, round(lat, 3), round(lon, 3))
    return resultado
