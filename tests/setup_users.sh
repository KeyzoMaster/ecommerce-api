#!/bin/bash

source "$(dirname "$0")/common.sh"

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}👤 SETUP : CRÉATION ET PROMOTION DES UTILISATEURS${NC}\n"
printf "${BLUE}====================================================${NC}\n"

register_user() {
    printf "Inscription de $1...\n"
    STATUS=$(curl -s -X POST -o /tmp/api_res.json -w "%{http_code}" "$BASE_URL/iam/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"username\": \"$1\", \"email\": \"$2\", \"password\": \"$3\"}")
    if [[ "$STATUS" == "201" ]]; then
        printf "${GREEN}[OK]${NC} $1 inscrit\n"
    else
        printf "${YELLOW}[INFO]${NC} $1 déjà inscrit ou erreur (Status: $STATUS)\n"
    fi
}

promote_user() {
    printf "Promotion de $1 au rôle $2...\n"
    docker exec ecommerce_postgres psql -U e_user -d ecommerce_db -c "SELECT promote_user('$1', '$2');" > /dev/null 2>&1
    printf "${GREEN}[OK]${NC} $1 promu\n"
}

# Inscription des 3 types d'utilisateurs
register_user "admin" "admin@ecommerce.local" "admin123"
register_user "client" "client@ecommerce.local" "client123"
register_user "owner" "owner@ecommerce.local" "owner123"

# Promotion manuelle (Bypass limitations API)
promote_user "admin" "ADMIN"
promote_user "client" "CLIENT"
promote_user "owner" "STORE_OWNER"

printf "\n✅ Fin du setup des utilisateurs.\n"
