# PowerShell script: export-keycloak-realm.ps1
$ErrorActionPreference = "Stop"

$container = "lab5-g07-keycloak-1"
$base = "/opt/keycloak"

# Crea la cartella nel container
docker exec $container mkdir -p "$base/realm"

# Esporta il realm
docker exec $container "$base/bin/kc.sh" export --dir "$base/realm" --realm ez_car_rent --users different_files

# Copia i dati dal container alla macchina host
docker cp "${container}:${base}/realm" .

# Rimuove la cartella temporanea nel container
docker exec $container rm -r "$base/realm"
