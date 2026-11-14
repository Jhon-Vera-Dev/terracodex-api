#!/usr/bin/env python3
"""Carga los usuarios iniciales en un terracodex-api desplegado, a través del propio API.

Idempotente: los usuarios que ya existen se saltan (no se modifican). Solo usa la librería estándar.

Las contraseñas nunca van en el JSON ni en el repositorio:
  - la del administrador: TERRACODEX_ADMIN_CLAVE o se pregunta
  - la inicial de los usuarios nuevos: TERRACODEX_CLAVE_INICIAL o se pregunta (8 a 72 caracteres, letras y números)

Uso:
  python3 scripts/seed/cargar_usuarios.py --api http://76.13.225.20
  python3 scripts/seed/cargar_usuarios.py --api http://localhost:8080 --archivo scripts/seed/usuarios.json
"""

import argparse
import getpass
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path


class ErrorApi(Exception):
    pass


def llamar(api, metodo, ruta, token=None, cuerpo=None):
    headers = {"Accept": "application/json"}
    datos = None
    if cuerpo is not None:
        headers["Content-Type"] = "application/json"
        datos = json.dumps(cuerpo).encode("utf-8")
    if token:
        headers["Authorization"] = f"Bearer {token}"
    peticion = urllib.request.Request(api + ruta, data=datos, method=metodo, headers=headers)
    try:
        with urllib.request.urlopen(peticion, timeout=20) as respuesta:
            return json.loads(respuesta.read()).get("data")
    except urllib.error.HTTPError as e:
        try:
            cuerpo_error = json.loads(e.read())
            mensaje = cuerpo_error.get("message", "")
            errores = cuerpo_error.get("errors") or []
            if errores:
                mensaje += " (" + "; ".join(f"{x.get('field')}: {x.get('message')}" for x in errores) + ")"
        except ValueError:
            mensaje = e.reason
        raise ErrorApi(f"{metodo} {ruta} → {e.code}: {mensaje}") from e
    except urllib.error.URLError as e:
        raise ErrorApi(f"No se pudo conectar con {api}: {e.reason}") from e


def clave(variable, pregunta):
    valor = os.environ.get(variable)
    return valor if valor else getpass.getpass(pregunta)


def main():
    parser = argparse.ArgumentParser(description="Carga usuarios iniciales en terracodex-api")
    parser.add_argument("--api", default="http://localhost:8080", help="URL base del API")
    parser.add_argument("--archivo", default=str(Path(__file__).with_name("usuarios.json")))
    parser.add_argument("--admin", default=os.environ.get("TERRACODEX_ADMIN_LOGIN", "admin"))
    args = parser.parse_args()
    api = args.api.rstrip("/")

    usuarios = json.loads(Path(args.archivo).read_text(encoding="utf-8"))["usuarios"]

    token = llamar(api, "POST", "/api/auth/login", cuerpo={
        "login": args.admin,
        "contrasena": clave("TERRACODEX_ADMIN_CLAVE", f"Contraseña de {args.admin}: "),
        "canal": "WEB",
    })["accessToken"]

    roles = {r["descripcion"]: r["id"] for r in llamar(api, "GET", "/api/seguridad/roles/opciones", token)}

    clave_inicial = None
    creados = saltados = 0
    for u in usuarios:
        login = u["login"].strip().lower()
        busqueda = urllib.parse.quote(login)
        existentes = llamar(api, "GET", f"/api/seguridad/usuarios?search={busqueda}&size=100", token)["content"]
        if any(e["login"] == login for e in existentes):
            print(f"  = {login}: ya existe, no se modifica")
            saltados += 1
            continue

        faltantes = [r for r in u["roles"] if r not in roles]
        if faltantes:
            raise ErrorApi(f"{login}: no existen o están inactivos los roles {faltantes}")

        if clave_inicial is None:
            clave_inicial = clave("TERRACODEX_CLAVE_INICIAL", "Contraseña inicial de los usuarios nuevos: ")

        resultado = llamar(api, "POST", "/api/seguridad/usuarios", token, {
            "login": login,
            "descripcion": u.get("descripcion") or login,
            "contrasena": clave_inicial,
            "isWeb": u.get("isWeb", True),
            "isMovil": u.get("isMovil", True),
            "rolIds": [roles[r] for r in u["roles"]],
        })
        print(f"  + {login}: creado (id {resultado['id']}, roles {', '.join(u['roles'])})")
        creados += 1

    print(f"Listo: {creados} creado(s), {saltados} ya existían.")


if __name__ == "__main__":
    try:
        main()
    except ErrorApi as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)
