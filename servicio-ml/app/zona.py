"""Zona (distrito, provincia, departamento) a partir de coordenadas, con Nominatim (OpenStreetMap).

Política de uso de Nominatim: máximo 1 petición por segundo y un User-Agent propio. Con la caché por coordenada
redondeada (~1 km) y el volumen de registros de campo es suficiente; si crece, cambiar a un proveedor de pago.
"""

import os
import threading
import time
from functools import lru_cache

import httpx

NOMINATIM_URL = os.getenv("NOMINATIM_URL", "https://nominatim.openstreetmap.org/reverse")
USER_AGENT = os.getenv("NOMINATIM_USER_AGENT", "terracodex-servicio-ml/1.0")

_client = httpx.Client(timeout=httpx.Timeout(8, connect=4), headers={"User-Agent": USER_AGENT})
_lock = threading.Lock()
_ultima = 0.0


def _primero(address: dict, *claves: str) -> str | None:
    for clave in claves:
        if address.get(clave):
            return address[clave]
    return None


@lru_cache(maxsize=2048)
def buscar_zona(lat: float, lon: float) -> dict | None:
    """``None`` si no hay conexión o el punto no tiene dirección (mar): la zona es informativa, no bloquea."""
    global _ultima
    with _lock:
        espera = 1.0 - (time.monotonic() - _ultima)
        if espera > 0:
            time.sleep(espera)
        _ultima = time.monotonic()
    try:
        response = _client.get(
            NOMINATIM_URL,
            params={"lat": lat, "lon": lon, "format": "jsonv2", "zoom": 14, "accept-language": "es"},
        )
        response.raise_for_status()
        address = response.json().get("address") or {}
    except (httpx.HTTPError, ValueError):
        return None
    if not address:
        return None

    # En Perú OSM etiqueta: state = departamento, province/county = provincia, city/town/village = distrito
    distrito = _primero(address, "city_district", "city", "town", "village", "municipality", "suburb", "hamlet")
    provincia = _primero(address, "province", "county", "state_district")
    departamento = _primero(address, "state", "region")
    pais = address.get("country")
    partes = [p for p in (distrito, provincia, departamento) if p]
    return {
        "descripcion": ", ".join(dict.fromkeys(partes)) or pais,
        "distrito": distrito,
        "provincia": provincia,
        "departamento": departamento,
        "pais": pais,
    }
