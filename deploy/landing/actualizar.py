"""Regenera deploy/landing desde la landing del frontend (D:\\TerraCode\\src\\assets\\landing).

    python deploy/landing/actualizar.py D:\\TerraCode [explorador.rapiditto.com]

La landing original vive dentro de la app TerraSprout y enlaza a sus rutas (../mapa, /asistente) y a archivos de la
carpeta de arriba (../earth-*.webp, ../terra-language.js...). Aquí se sirve sola en la raíz del dominio, así que:

- copia también esos archivos de la carpeta de arriba y quita el "../";
- el mapa y el asistente apuntan al explorador (https://<explorador>/mapa y /asistente);
- "Registrarse" / "Iniciar sesión" (deshabilitados en el original) llevan al APK y a la app web (/app/).

Si cambia la landing original, vuelve a correr este script. Falla si un reemplazo ya no encuentra su texto: así no se
publica una landing a medio adaptar.
"""

import re
import shutil
import sys
from pathlib import Path

DESTINO = Path(__file__).resolve().parent
CONSERVAR = {"actualizar.py", "LEEME.md", "privacidad.html"}
# Archivos de src/assets que usa la landing
DE_ARRIBA = ["space.svg", "terra-language.css", "terra-language.js", "assistant-globe.css", "assistant-launcher.js",
             "terra-bot-reference.png"]


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    assets = Path(sys.argv[1]) / "src" / "assets"
    origen = assets / "landing"
    explorador = "https://" + (sys.argv[2] if len(sys.argv) > 2 else "explorador.rapiditto.com").strip("/")
    if not (origen / "inicio.html").is_file():
        sys.exit(f"No encuentro {origen / 'inicio.html'}")

    for f in DESTINO.iterdir():
        if f.name not in CONSERVAR:
            f.unlink() if f.is_file() else shutil.rmtree(f)
    for f in origen.iterdir():
        if f.is_file() and f.name not in ("chrome.html", "inicio.html"):
            shutil.copy2(f, DESTINO / f.name)
    for nombre in DE_ARRIBA:
        shutil.copy2(assets / nombre, DESTINO / nombre)
    # Fotogramas del globo que usa journey.js (cada 15°)
    for angulo in range(-180, 180, 15):
        shutil.copy2(assets / f"earth-{angulo}.webp", DESTINO / f"earth-{angulo}.webp")

    html = (origen / "inicio.html").read_text(encoding="utf-8")
    (DESTINO / "index.html").write_text(html, encoding="utf-8", newline="\n")

    def cambiar(archivo, pares, regex=()):
        p = DESTINO / archivo
        s = p.read_text(encoding="utf-8")
        for viejo, nuevo in pares:
            if viejo not in s:
                sys.exit(f"{archivo}: ya no encuentro {viejo!r}; revisa la landing original y ajusta este script")
            s = s.replace(viejo, nuevo)
        for patron, nuevo in regex:
            s, n = re.subn(patron, nuevo, s)
            if not n:
                sys.exit(f"{archivo}: ya no encuentro el patrón {patron!r}")
        p.write_text(s, encoding="utf-8", newline="\n")

    cambiar("index.html", [
        ('href="../mapa?accion=ubicacion" target="_top">', f'href="{explorador}/mapa?accion=ubicacion">'),
        ('href="../mapa?accion=buscar" target="_top">', f'href="{explorador}/mapa?accion=buscar">'),
        ('<a href="../mapa" target="_top">', f'<a href="{explorador}/mapa">'),
        ('href="/asistente"', f'href="{explorador}/asistente"'),
    ], regex=[(r'(src|href)="\.\./(?!mapa)', r'\1="')])
    cambiar("journey.js", [("'../earth-'", "'earth-'")])
    cambiar("styles.css", [('url("../space.svg")', 'url("space.svg")')])
    cambiar("site-chrome.js", [
        ('url("../mapa")', f'"{explorador}/mapa"'),
    ], regex=[
        # Botones deshabilitados del encabezado → enlaces al APK y a la app web
        (r'<button\s+type="button"\s+class="header-button"\s+disabled>',
         '<a class="header-button" href="/descargas/terracode.apk">'),
        (r'<button\s+type="button"\s+class="header-button header-button-primary"\s+disabled>',
         '<a class="header-button header-button-primary" href="/app/">'),
        (r'(\$\{tr\("Registrarse", "Sign up"\)\}\s*)</button>', r'\1</a>'),
        (r'(\$\{tr\("Iniciar sesión", "Sign in"\)\}\s*)</button>', r'\1</a>'),
    ])
    cambiar("site-chrome.js", [], regex=[(r'"Registrarse", "Sign up"', '"Descargar APK", "Get the APK"')])
    with open(DESTINO / "styles.css", "a", encoding="utf-8", newline="\n") as f:
        f.write("\n/* Despliegue: botones del encabezado como enlaces */\n"
                "a.header-button { display: inline-flex; align-items: center; text-decoration: none; }\n")

    restos = [f.name for f in DESTINO.glob("*.*") if f.suffix in (".html", ".js", ".css")
              and re.search(r'(src|href)="\.\./|url\("\.\./', f.read_text(encoding="utf-8"))]
    print(f"Landing actualizada en {DESTINO} ({len(list(DESTINO.iterdir()))} archivos)."
          + (f" Revisa rutas ../ en: {', '.join(restos)}" if restos else ""))


if __name__ == "__main__":
    main()
