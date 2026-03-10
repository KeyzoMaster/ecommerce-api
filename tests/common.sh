#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080/api/v1"
RES_FILE="/tmp/api_res.json"
HDR_FILE="/tmp/api_hdrs.txt"

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Utilitaires
check_status() {
    local status=$1
    local expected=$2
    local message=$3
    if [[ "$status" == "$expected" ]]; then
        printf "${GREEN}[OK]${NC} $message (Status: $status)\n"
    else
        printf "${RED}[FAIL]${NC} $message (Attendu: $expected, Obtenu: $status)\n"
        if [ -f "$RES_FILE" ]; then
            jq . "$RES_FILE" 2>/dev/null || cat "$RES_FILE"
            printf "\n"
        fi
        exit 1
    fi
}

get_token_response() {
    local email=$1
    local password=$2
    curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"identifier\": \"$email\", \"password\": \"$password\"}"
}

# Chargement initial des tokens pour les tests de domaine
init_tokens() {
    printf "${BLUE}--- 🔑 Authentification des rôles ---${NC}\n"
    
    local admin_res=$(get_token_response "admin@ecommerce.local" "admin123")
    ADMIN_TOKEN=$(echo "$admin_res" | jq -r '.accessToken // empty')
    
    local client_res=$(get_token_response "client@ecommerce.local" "client123")
    CLIENT_TOKEN=$(echo "$client_res" | jq -r '.accessToken // empty')
    CLIENT_REFRESH=$(echo "$client_res" | jq -r '.refreshToken // empty')
    
    local owner_res=$(get_token_response "owner@ecommerce.local" "owner123")
    OWNER_TOKEN=$(echo "$owner_res" | jq -r '.accessToken // empty')

    if [[ -z "$ADMIN_TOKEN" || -z "$CLIENT_TOKEN" ]]; then
        echo -e "${RED}Erreur: Impossible de récupérer les tokens. Vérifiez que le serveur tourne et que le seeding est fait.${NC}"
        exit 1
    fi
}
