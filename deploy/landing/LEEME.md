# Landing publicada

Copia de `src/assets/landing` del frontend (D:\TerraCode, viene de TerraSprout) adaptada para servirse sola con
Caddy en la raíz del dominio. **No la edites a mano**: regénérala después de cada pull del frontend:

```
python deploy/landing/actualizar.py D:\TerraCode explorador.rapiditto.com
```

El script copia la landing y los archivos de `src/assets` que usa, y cambia los enlaces: el mapa y el asistente van
al explorador (`https://explorador.../mapa` y `/asistente`); «Descargar APK» e «Iniciar sesión» van a
`/descargas/terracode.apk` y a `/app/`. Si la landing original cambia tanto que un reemplazo ya no aplica, el
script se detiene y dice cuál.
