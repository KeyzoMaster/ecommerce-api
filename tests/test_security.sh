#!/bin/bash

source "$(dirname "$0")/common.sh"

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}🛡️  TESTS DE SÉCURITÉ (AUTHENTIFICATION & PROTECTIONS)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# 1. Test Login & Token Generation
printf "\n--- 🔐 Test Login & Tokens ---\n"
get_token_response "client@ecommerce.local" "client123" > /dev/null
ACCESS_TOKEN=$(jq -r '.accessToken // empty' "$RES_FILE")
REFRESH_TOKEN=$(jq -r '.refreshToken // empty' "$RES_FILE")

if [[ -n "$ACCESS_TOKEN" ]]; then
    printf "${GREEN}[OK]${NC} Génération Access Token\n"
else
    printf "${RED}[FAIL]${NC} Génération Access Token\n"
    exit 1
fi

if [[ -n "$REFRESH_TOKEN" ]]; then
    printf "${GREEN}[OK]${NC} Génération Refresh Token\n"
else
    printf "${RED}[FAIL]${NC} Génération Refresh Token\n"
    exit 1
fi

# 2. Test Refresh Token
printf "\n--- 🔄 Test Refresh Token ---\n"
REFRESH_RES=$(curl -s -X POST "$BASE_URL/iam/auth/refresh?token=$REFRESH_TOKEN")
NEW_ACCESS_TOKEN=$(echo "$REFRESH_RES" | jq -r '.accessToken // empty')

if [[ -n "$NEW_ACCESS_TOKEN" ]]; then
    printf "${GREEN}[OK]${NC} Rafraîchissement Token\n"
else
    printf "${RED}[FAIL]${NC} Rafraîchissement Token\n"
    echo "$REFRESH_RES"
    exit 1
fi

# 3. Test Access Control (PBAC)
printf "\n--- 🚫 Test Contrôle d'Accès (PBAC) ---\n"
# Client essaie d'accéder au dashboard admin
ADMIN_RES_STATUS=$(curl -s -o /tmp/api_res.json -w "%{http_code}" "$BASE_URL/analytics/dashboard" \
    -H "Authorization: Bearer $ACCESS_TOKEN")
check_status "$ADMIN_RES_STATUS" "403" "Refus accès Admin pour Client"

# 4. Test Révocation (Logout)
printf "\n--- 🚪 Test Logout & Révocation ---\n"
curl -s -X POST "$BASE_URL/iam/auth/logout" -H "Authorization: Bearer $NEW_ACCESS_TOKEN" > /dev/null
# Vérifier que le token ne fonctionne plus
PROTECTED_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/iam/users/me" \
    -H "Authorization: Bearer $NEW_ACCESS_TOKEN")
check_status "$PROTECTED_STATUS" "401" "Token révoqué après logout"

# 5. Test Rate Limiting
printf "\n--- ⏳ Test Rate Limiting (Brute Force Protection) ---\n"
printf "Exécution de requêtes rapides...\n"
SUCCESS_COUNT=0
LIMIT_REACHED=false

for i in {1..20}; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/iam/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"identifier": "invalid", "password": "wrong"}')
    if [[ "$STATUS" == "429" ]]; then
        LIMIT_REACHED=true
        break
    fi
done

if [[ "$LIMIT_REACHED" == "true" ]]; then
    printf "${GREEN}[OK]${NC} Rate limit activé (Status 429 reçu)\n"
else
    printf "${YELLOW}[WARN]${NC} Rate limit non atteint (Peut-être seuil trop haut ou non configuré)\n"
fi

printf "\n✅ Fin des tests de sécurité.\n"
