#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}📊 TESTS ANALYTICS & DASHBOARD${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# 1. Consultation Dashboard (Admin)
printf "\n--- 📈 Test Dashboard (Admin) ---\n"
DASH_RES=$(curl -s "$BASE_URL/analytics/dashboard" -H "Authorization: Bearer $ADMIN_TOKEN")
check_status "$(echo "$DASH_RES" | jq -r '.totalRevenue != null')" "true" "Accès Dashboard Admin"

# 2. Export CSV Top Produits
printf "\n--- 📄 Test Export Top Produits (CSV) ---\n"
STATUS=$(curl -s -o /tmp/top_prods.csv -w "%{http_code}" "$BASE_URL/analytics/export/top-products" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
check_status "$STATUS" "200" "Export CSV Top Produits"
if [[ -s /tmp/top_prods.csv ]]; then
    printf "${GREEN}[OK]${NC} Fichier CSV non vide\n"
else
    printf "${RED}[FAIL]${NC} Fichier CSV vide\n"
    exit 1
fi

# 3. Export CSV Tendances
printf "\n--- 📄 Test Export Tendances (CSV) ---\n"
STATUS=$(curl -s -o /tmp/trends.csv -w "%{http_code}" "$BASE_URL/analytics/export/daily-trends" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
check_status "$STATUS" "200" "Export CSV Tendances"

# 4. Vérification Sécurité (Client ne doit pas accéder aux analytics)
printf "\n--- 🔐 Test Sécurité ---\n"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/analytics/dashboard" \
    -H "Authorization: Bearer $CLIENT_TOKEN")
check_status "$STATUS" "403" "Interdiction accès Dashboard pour Client"

printf "\n✅ Fin des tests analytics.\n"
