#!/usr/bin/env bash
# =============================================================================
# test.sh — Tests d'intégration automatiques
# Projet 2 — Planification & Logistique — UMP Oujda
# =============================================================================

# Note: pas de "set -e" — le script doit afficher tous les PASS/FAIL même si une étape échoue
set -uo pipefail

CATALOGUE_URL="${CATALOGUE_URL:-http://localhost:8081}"
LOCAUX_URL="${LOCAUX_URL:-http://localhost:8082}"
EMPLOI_URL="${EMPLOI_URL:-http://localhost:8083}"
CURL_OPTS=(--connect-timeout 10 --max-time 120 -s)

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASS_COUNT=0
FAIL_COUNT=0

pass() {
    PASS_COUNT=$((PASS_COUNT + 1))
    echo -e "${GREEN}[PASS]${NC} $1"
}

fail() {
    FAIL_COUNT=$((FAIL_COUNT + 1))
    echo -e "${RED}[FAIL]${NC} $1"
    if [[ -n "${2:-}" ]]; then
        echo -e "${RED}       Détail: $2${NC}"
    fi
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Attend qu'une URL réponde avec un code HTTP donné (défaut 200)
wait_for_service() {
    local name="$1"
    local url="$2"
    local expected="${3:-200}"
    local max_attempts="${4:-60}"

    info "Attente de ${name} (${url})..."
    for ((i = 1; i <= max_attempts; i++)); do
        http_code=$(curl "${CURL_OPTS[@]}" -o /dev/null -w "%{http_code}" "${url}" 2>/dev/null || echo "000")
        http_code=$(echo "${http_code}" | tr -d '\r')
        if [[ "${http_code}" == "${expected}" ]]; then
            pass "${name} prêt (HTTP ${http_code}, tentative ${i}/${max_attempts})"
            return 0
        fi
        sleep 2
    done
    fail "${name} indisponible après ${max_attempts} tentatives" "dernier code HTTP: ${http_code}"
    return 1
}

# Sépare corps JSON et code HTTP (dernière ligne)
parse_response() {
    local resp="$1"
    resp=$(echo "${resp}" | tr -d '\r')
    HTTP_CODE=$(echo "${resp}" | tail -n 1)
    HTTP_BODY=$(echo "${resp}" | head -n -1)
}

# Extrait la valeur d'un champ JSON simple (ex: "id") — ne doit jamais faire échouer le script (Git Bash + set -u)
json_field() {
    local json="$1"
    local field="$2"
    local value=""
    if [[ -n "${json}" ]]; then
        value=$(echo "${json}" | grep -oE "\"${field}\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" 2>/dev/null | head -1 | sed -E 's/.*:[[:space:]]*\"|\"$//g' || true)
        if [[ -z "${value}" ]]; then
            value=$(echo "${json}" | grep -oE "\"${field}\"[[:space:]]*:[[:space:]]*[0-9]+" 2>/dev/null | head -1 | sed -E 's/.*:[[:space:]]*//' | tr -d ' "' || true)
        fi
    fi
    echo "${value}"
}

echo ""
echo "============================================================"
echo "  Tests Projet 2 — Planification & Logistique (UMP Oujda)"
echo "============================================================"
echo ""

# -----------------------------------------------------------------------------
# Étape 0 — Attente des services
# -----------------------------------------------------------------------------
wait_for_service "service-catalogue" "${CATALOGUE_URL}/api/cours" "200" 60
wait_for_service "service-locaux" "${LOCAUX_URL}/api/locaux" "200" 60
wait_for_service "service-emploi-du-temps" "${EMPLOI_URL}/api/emploi-du-temps" "200" 60

SUFFIX=$(date +%s)
# code cours : VARCHAR(20) — éviter "TEST-COURS-" + 10 chiffres (21 caractères)
COURS_CODE="TST-${SUFFIX}"
LOCAL_CODE="LOC-${SUFFIX}"

# -----------------------------------------------------------------------------
# Étape 1 — Créer un cours
# -----------------------------------------------------------------------------
info "Création d'un cours via POST..."
COURS_BODY="{\"code\":\"${COURS_CODE}\",\"intitule\":\"Test Integration Cloud\",\"syllabus\":\"Test auto\",\"prerequis\":\"INF301\",\"credits\":4}"
COURS_RESP=$(curl "${CURL_OPTS[@]}" -w "\n%{http_code}" -X POST "${CATALOGUE_URL}/api/cours" \
    -H "Content-Type: application/json" \
    -d "${COURS_BODY}")
parse_response "${COURS_RESP}"
COURS_HTTP="${HTTP_CODE}"
COURS_JSON="${HTTP_BODY}"
COURS_ID=$(json_field "${COURS_JSON}" "id")

if [[ "${COURS_HTTP}" == "201" && -n "${COURS_ID}" ]]; then
    pass "POST cours → HTTP 201, id=${COURS_ID}"
else
    fail "POST cours → attendu HTTP 201" "reçu ${COURS_HTTP}, body=${COURS_JSON}"
fi

# -----------------------------------------------------------------------------
# Étape 2 — Créer un local
# -----------------------------------------------------------------------------
info "Création d'un local via POST..."
LOCAL_BODY="{\"code\":\"${LOCAL_CODE}\",\"nom\":\"Salle Test Integration\",\"type\":\"SALLE_TP\",\"capacite\":35,\"batiment\":\"Bloc Test\",\"etage\":1,\"projecteur\":true,\"tableauNumerique\":false,\"climatisation\":true,\"accessiblePMR\":true,\"disponibilite\":\"DISPONIBLE\"}"
LOCAL_RESP=$(curl "${CURL_OPTS[@]}" -w "\n%{http_code}" -X POST "${LOCAUX_URL}/api/locaux" \
    -H "Content-Type: application/json" \
    -d "${LOCAL_BODY}")
parse_response "${LOCAL_RESP}"
LOCAL_HTTP="${HTTP_CODE}"
LOCAL_JSON="${HTTP_BODY}"
LOCAL_ID=$(json_field "${LOCAL_JSON}" "id")

if [[ "${LOCAL_HTTP}" == "201" && -n "${LOCAL_ID}" ]]; then
    pass "POST local → HTTP 201, id=${LOCAL_ID}"
else
    fail "POST local → attendu HTTP 201" "reçu ${LOCAL_HTTP}, body=${LOCAL_JSON}"
fi

# -----------------------------------------------------------------------------
# Étape 3 — Planifier une séance (201)
# -----------------------------------------------------------------------------
if [[ -z "${COURS_ID}" || -z "${LOCAL_ID}" ]]; then
    fail "Planification séance" "cours ou local non créé (COURS_ID=${COURS_ID}, LOCAL_ID=${LOCAL_ID})"
else
info "Planification d'une séance..."
SEANCE_BODY="{\"coursId\":${COURS_ID},\"localId\":${LOCAL_ID},\"jour\":\"MARDI\",\"heureDebut\":\"14:00\",\"heureFin\":\"16:00\",\"semestre\":\"S2-2025\",\"type\":\"COURS_MAGISTRAL\"}"
SEANCE_RESP=$(curl "${CURL_OPTS[@]}" -w "\n%{http_code}" -X POST "${EMPLOI_URL}/api/emploi-du-temps" \
    -H "Content-Type: application/json" \
    -d "${SEANCE_BODY}")
parse_response "${SEANCE_RESP}"
SEANCE_HTTP="${HTTP_CODE}"
SEANCE_JSON="${HTTP_BODY}"
SEANCE_ID=$(json_field "${SEANCE_JSON}" "id")

if [[ "${SEANCE_HTTP}" == "201" && -n "${SEANCE_ID}" ]]; then
    pass "POST séance → HTTP 201, id=${SEANCE_ID}"
else
    fail "POST séance → attendu HTTP 201" "reçu ${SEANCE_HTTP}, body=${SEANCE_JSON}"
fi
fi

# -----------------------------------------------------------------------------
# Étape 4 — Conflit horaire (409)
# La salle est OCCUPE après planification ; on la repasse DISPONIBLE pour
# franchir la vérification inter-services et déclencher le conflit en base.
# -----------------------------------------------------------------------------
if [[ -n "${SEANCE_ID}" && -n "${COURS_ID}" && -n "${LOCAL_ID}" ]]; then
info "Test conflit horaire (même salle, même jour, créneau chevauchant)..."
PATCH_HTTP=$(curl "${CURL_OPTS[@]}" -o /dev/null -w "%{http_code}" -X PATCH \
    "${LOCAUX_URL}/api/locaux/${LOCAL_ID}/disponibilite?valeur=DISPONIBLE" | tr -d '\r')
if [[ "${PATCH_HTTP}" != "200" ]]; then
    warn "PATCH DISPONIBLE avant test 409 → HTTP ${PATCH_HTTP}"
fi
CONFLICT_BODY="{\"coursId\":${COURS_ID},\"localId\":${LOCAL_ID},\"jour\":\"MARDI\",\"heureDebut\":\"15:00\",\"heureFin\":\"17:00\",\"semestre\":\"S2-2025\",\"type\":\"TD\"}"
CONFLICT_RESP=$(curl "${CURL_OPTS[@]}" -w "\n%{http_code}" -X POST "${EMPLOI_URL}/api/emploi-du-temps" \
    -H "Content-Type: application/json" \
    -d "${CONFLICT_BODY}")
parse_response "${CONFLICT_RESP}"
CONFLICT_HTTP="${HTTP_CODE}"

if [[ "${CONFLICT_HTTP}" == "409" ]]; then
    pass "POST séance conflictuelle → HTTP 409"
else
    fail "POST séance conflictuelle → attendu HTTP 409" "reçu ${CONFLICT_HTTP}"
fi
else
    fail "Test conflit horaire" "séance non planifiée — étape ignorée"
fi

# -----------------------------------------------------------------------------
# Étape 5 — GET séances par jour
# -----------------------------------------------------------------------------
info "Vérification GET /emploi-du-temps/jour/MARDI..."
JOUR_RESP=$(curl "${CURL_OPTS[@]}" -w "\n%{http_code}" "${EMPLOI_URL}/api/emploi-du-temps/jour/MARDI")
parse_response "${JOUR_RESP}"
JOUR_HTTP="${HTTP_CODE}"
JOUR_JSON="${HTTP_BODY}"

if [[ "${JOUR_HTTP}" == "200" && "${JOUR_JSON}" == *"\"id\":${SEANCE_ID}"* || "${JOUR_JSON}" == *"\"id\": ${SEANCE_ID}"* ]]; then
    pass "GET jour/MARDI contient la séance id=${SEANCE_ID}"
elif [[ "${JOUR_HTTP}" == "200" && "${JOUR_JSON}" == *"${SEANCE_ID}"* ]]; then
    pass "GET jour/MARDI contient la séance id=${SEANCE_ID}"
else
    fail "GET jour/MARDI → séance introuvable" "HTTP ${JOUR_HTTP}, body=${JOUR_JSON}"
fi

# -----------------------------------------------------------------------------
# Étape 6 — DELETE séance (204)
# -----------------------------------------------------------------------------
if [[ -n "${SEANCE_ID}" ]]; then
info "Suppression de la séance id=${SEANCE_ID}..."
DELETE_HTTP=$(curl "${CURL_OPTS[@]}" -o /dev/null -w "%{http_code}" -X DELETE "${EMPLOI_URL}/api/emploi-du-temps/${SEANCE_ID}" | tr -d '\r')

if [[ "${DELETE_HTTP}" == "204" ]]; then
    pass "DELETE séance → HTTP 204"
else
    fail "DELETE séance → attendu HTTP 204" "reçu ${DELETE_HTTP}"
fi
else
    fail "DELETE séance" "id séance manquant — étape ignorée"
fi

# -----------------------------------------------------------------------------
# Étape 7 — Vérifier salle DISPONIBLE
# -----------------------------------------------------------------------------
info "Vérification disponibilité du local id=${LOCAL_ID}..."
LOCAL_GET=$(curl "${CURL_OPTS[@]}" -w "\n%{http_code}" "${LOCAUX_URL}/api/locaux/${LOCAL_ID}")
parse_response "${LOCAL_GET}"
LOCAL_GET_HTTP="${HTTP_CODE}"
LOCAL_GET_JSON="${HTTP_BODY}"
DISPO=$(json_field "${LOCAL_GET_JSON}" "disponibilite")

if [[ "${LOCAL_GET_HTTP}" == "200" && "${DISPO}" == "DISPONIBLE" ]]; then
    pass "Local ${LOCAL_ID} → disponibilite=DISPONIBLE"
else
    fail "Local ${LOCAL_ID} → attendu DISPONIBLE" "HTTP ${LOCAL_GET_HTTP}, disponibilite=${DISPO}"
fi

# -----------------------------------------------------------------------------
# Tests endpoints catalogue (lecture)
# -----------------------------------------------------------------------------
info "Tests endpoints catalogue..."
if [[ -n "${COURS_ID}" ]]; then
HTTP=$(curl "${CURL_OPTS[@]}" -o /dev/null -w "%{http_code}" "${CATALOGUE_URL}/api/cours/${COURS_ID}" | tr -d '\r')
if [[ "${HTTP}" == "200" ]]; then pass "GET /api/cours/${COURS_ID} → 200"; else fail "GET /api/cours/${COURS_ID}" "HTTP ${HTTP}"; fi
fi

HTTP=$(curl "${CURL_OPTS[@]}" -o /dev/null -w "%{http_code}" "${CATALOGUE_URL}/api/cours/code/${COURS_CODE}" | tr -d '\r')
if [[ "${HTTP}" == "200" ]]; then pass "GET /api/cours/code/${COURS_CODE} → 200"; else fail "GET /api/cours/code/${COURS_CODE}" "HTTP ${HTTP}"; fi

# -----------------------------------------------------------------------------
# Tests endpoints locaux (lecture)
# -----------------------------------------------------------------------------
info "Tests endpoints locaux..."
HTTP=$(curl "${CURL_OPTS[@]}" -o /dev/null -w "%{http_code}" "${LOCAUX_URL}/api/locaux/disponibles?capaciteMin=30" | tr -d '\r')
if [[ "${HTTP}" == "200" ]]; then pass "GET /api/locaux/disponibles?capaciteMin=30 → 200"; else fail "GET disponibles" "HTTP ${HTTP}"; fi

HTTP=$(curl "${CURL_OPTS[@]}" -o /dev/null -w "%{http_code}" "${LOCAUX_URL}/api/locaux/type/SALLE_TP" | tr -d '\r')
if [[ "${HTTP}" == "200" ]]; then pass "GET /api/locaux/type/SALLE_TP → 200"; else fail "GET type/SALLE_TP" "HTTP ${HTTP}"; fi

# -----------------------------------------------------------------------------
# Nettoyage optionnel des données de test
# -----------------------------------------------------------------------------
info "Nettoyage des ressources de test..."
[[ -n "${COURS_ID}" ]] && curl "${CURL_OPTS[@]}" -o /dev/null -X DELETE "${CATALOGUE_URL}/api/cours/${COURS_ID}" || true
[[ -n "${LOCAL_ID}" ]] && curl "${CURL_OPTS[@]}" -o /dev/null -X DELETE "${LOCAUX_URL}/api/locaux/${LOCAL_ID}" || true
warn "Ressources test cours=${COURS_ID} et local=${LOCAL_ID} supprimées si DELETE autorisé"

# -----------------------------------------------------------------------------
# Résumé
# -----------------------------------------------------------------------------
echo ""
echo "============================================================"
if [[ "${FAIL_COUNT}" -eq 0 ]]; then
    echo -e "  ${GREEN}RÉSULTAT GLOBAL : SUCCÈS${NC} — ${PASS_COUNT} tests réussis"
else
    echo -e "  ${RED}RÉSULTAT GLOBAL : ÉCHEC${NC} — ${PASS_COUNT} pass, ${FAIL_COUNT} fail"
fi
echo "============================================================"
echo ""

if [[ "${FAIL_COUNT}" -gt 0 ]]; then
    exit 1
fi
exit 0
