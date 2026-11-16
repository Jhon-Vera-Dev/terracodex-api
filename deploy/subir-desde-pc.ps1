# Despliega desde la PC (Windows) al VPS sin git pull en el servidor: empaqueta el último commit de la API y del
# frontend, los sube por scp y los aplica por ssh.
#
#   powershell -ExecutionPolicy Bypass -File D:\terracodex-api\deploy\subir-desde-pc.ps1
#   ... -Apk                      # además recompila el APK (tarda unos minutos)
#   ... -SoloWeb                  # solo la versión web (no reconstruye api ni ml)
#
# Sube lo que está en el último commit (no los cambios sin guardar). deploy/.env y deploy/descargas del servidor
# no se tocan: no están en el repositorio.
param(
    [string]$Servidor = "root@76.13.225.20",
    [string]$Destino = "/opt/terracodex",
    [string]$Api = "D:\terracodex-api",
    [string]$Frontend = "D:\TerraCode",
    [switch]$Apk,
    [switch]$SoloWeb,
    [int]$Intentos = 4
)
$ErrorActionPreference = "Stop"

foreach ($repo in @($Api, $Frontend)) {
    if (git -C $repo status --porcelain --untracked-files=no) {
        Write-Warning "$repo tiene cambios sin commit: no se suben (solo va el último commit)."
    }
}

$tmp = Join-Path $env:TEMP "terracodex-deploy"
New-Item -ItemType Directory -Force $tmp | Out-Null
$apiTar = Join-Path $tmp "terracodex-api.tar.gz"
$appZip = Join-Path $tmp "terracode-src.zip"

# core.autocrlf=false: los .sh deben llegar con fin de línea de Linux
Write-Host "Empaquetando la API ($(git -C $Api log --oneline -1))..."
git -C $Api -c core.autocrlf=false archive --format=tar.gz -o $apiTar HEAD
Write-Host "Empaquetando el frontend ($(git -C $Frontend log --oneline -1))..."
# Solo lo que usan el APK y la web (sin data/ ni los modelos viejos: pesan ~200 MB)
git -C $Frontend -c core.autocrlf=false archive --format=zip -o $appZip HEAD src web pyproject.toml README.md models/crop_model_v4.joblib

# La conexión al VPS a veces se corta: se reintenta (subir y aplicar se pueden repetir sin problema)
$opcionesSsh = @("-o", "ConnectTimeout=20", "-o", "ServerAliveInterval=15", "-o", "ServerAliveCountMax=4")
# $SoloConexion: ssh devuelve 255 si falla la conexión; otro código es un error del despliegue y no se reintenta
function Reintentar([string]$nombre, [scriptblock]$accion, [switch]$SoloConexion) {
    for ($intento = 1; $intento -le $Intentos; $intento++) {
        & $accion
        if ($LASTEXITCODE -eq 0) { return }
        if ($SoloConexion -and $LASTEXITCODE -ne 255) {
            throw "El despliegue falló en el servidor (revisa el mensaje de arriba)"
        }
        if ($intento -lt $Intentos) {
            Write-Warning "$nombre falló (intento $intento de $Intentos); se reintenta en 10 s..."
            Start-Sleep -Seconds 10
        }
    }
    throw "$nombre falló $Intentos veces: revisa tu conexión a internet"
}

Write-Host "Subiendo a $Servidor..."
Reintentar "La subida (scp)" { scp @opcionesSsh $apiTar $appZip "${Servidor}:/tmp/" }

$pasos = @(
    "set -e",
    "mkdir -p $Destino && tar -xzf /tmp/terracodex-api.tar.gz -C $Destino",
    "cd $Destino"
)
if (-not $SoloWeb) { $pasos += "docker compose -f deploy/docker-compose.yml up -d --build api ml" }
# publicar-web.sh valida el Caddyfile antes de recrear o recargar Caddy
$pasos += "bash deploy/web/publicar-web.sh /tmp/terracode-src.zip"
if ($Apk) { $pasos += "bash deploy/apk/construir-apk.sh /tmp/terracode-src.zip" }
$pasos += "docker compose -f deploy/docker-compose.yml --profile web ps"

Write-Host "Aplicando en el servidor..."
Reintentar "El despliegue en el servidor (ssh)" { ssh @opcionesSsh $Servidor ($pasos -join " && ") } -SoloConexion
Write-Host "Listo."
