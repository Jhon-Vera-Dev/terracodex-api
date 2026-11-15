"""Prueba el modelo real sin red: NASA POWER y Nominatim se reemplazan por valores fijos."""

import pytest
from fastapi.testclient import TestClient

from app import clima, main, zona

# Clima de referencia aproximado de Ica (costa sur del Perú)
ICA = {
    "temperature_mean": 20.5,
    "temperature_min_monthly_mean": 14.8,
    "temperature_max_monthly_mean": 27.9,
    "precipitation_annual_mm": 15.0,
    "air_humidity": 70.0,
    "surface_soil_wetness": 0.16,
    "root_soil_wetness": 0.18,
    "wind_speed": 3.1,
}


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(clima, "variables_historicas", lambda lat, lon: ICA)
    monkeypatch.setattr(zona, "buscar_zona", lambda lat, lon: {"descripcion": "Ica, Ica, Ica", "distrito": "Ica",
                                                                "provincia": "Ica", "departamento": "Ica",
                                                                "pais": "Perú"})
    with TestClient(main.app) as c:
        yield c


def test_recomienda_cultivos_ordenados(client):
    r = client.post("/recomendar", json={"latitud": -14.06, "longitud": -75.73, "tipo_riego": "irrigated_drip"})
    assert r.status_code == 200
    data = r.json()
    puntajes = [x["puntaje"] for x in data["recomendaciones"]]
    assert len(puntajes) == 8  # cultivos de goteo del modelo v4
    assert puntajes == sorted(puntajes, reverse=True)
    assert all(0 <= p <= 100 for p in puntajes)
    assert data["zona"]["departamento"] == "Ica"
    assert data["periodo_referencia"] == "1981-2010"


def test_valida_coordenadas_y_riego(client):
    assert client.post("/recomendar", json={"latitud": 120, "longitud": 0}).status_code == 422
    assert client.post("/recomendar", json={"latitud": 0, "longitud": 0, "tipo_riego": "x"}).status_code == 422


def test_nasa_caida_responde_503(client, monkeypatch):
    def caida(lat, lon):
        raise clima.DatosNoDisponibles("sin datos")

    monkeypatch.setattr(clima, "variables_historicas", caida)
    assert client.post("/recomendar", json={"latitud": -14, "longitud": -75}).status_code == 503


def test_clave_de_servicio(client, monkeypatch):
    monkeypatch.setattr(main, "API_KEY", "secreta")
    cuerpo = {"latitud": -14.06, "longitud": -75.73}
    assert client.post("/recomendar", json=cuerpo).status_code == 401
    assert client.post("/recomendar", json=cuerpo, headers={"X-Api-Key": "secreta"}).status_code == 200


def test_calculo_de_variables_nasa():
    meses = {f"{y}{m:02d}": 1.0 for y in range(1981, 2011) for m in range(1, 13)}
    data = {"header": {"fill_value": -999}, "properties": {"parameter": {k: meses for k in clima.METRICS}}}
    f = clima._calcular(data)
    assert f["temperature_mean"] == 1.0
    # 1 mm/día: 30 años con 7 bisiestos (1984…2008) → 10957 días / 30
    assert f["precipitation_annual_mm"] == pytest.approx(10957 / 30)
