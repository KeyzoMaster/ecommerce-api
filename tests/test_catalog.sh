#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}📦 TESTS DU CATALOGUE (PRODUITS & CATÉGORIES)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# ID de la boutique par défaut (issu de V4__Initial_Data.sql)
STORE_ID="018e2345-6789-7000-7000-000000000001"

# Génération d'un suffixe unique (Timestamp nanosecondes + Aléatoire)
UNIQUE_SUFFIX="$(date +%s%N)-${RANDOM}"

# 1. Création de Catégorie
printf "\n--- 📂 Test Catégorie ---\n"
CAT_NAME="Test Cat ${UNIQUE_SUFFIX}"
CAT_SLUG="test-cat-${UNIQUE_SUFFIX}"

CAT_RES=$(curl -s -X POST "$BASE_URL/catalog/categories" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"$CAT_NAME\",
        \"slug\": \"$CAT_SLUG\",
        \"description\": \"Description de test générée dynamiquement\"
    }")

CAT_ID=$(echo "$CAT_RES" | jq -r '.data.id // empty')

if [[ -n "$CAT_ID" ]]; then
    check_status "$(echo "$CAT_RES" | jq -r '.data.name')" "$CAT_NAME" "Création Catégorie"
else
    printf "${RED}[FAIL]${NC} Création Catégorie échouée (Conflit possible ou erreur serveur)\n"
    echo "$CAT_RES" | jq .
    exit 1
fi

# 2. Création de Produit
printf "\n--- 🏷️  Test Produit ---\n"
PROD_NAME="Produit Test ${UNIQUE_SUFFIX}"
PROD_SLUG="prod-test-${UNIQUE_SUFFIX}"
PROD_SKU="SKU-${UNIQUE_SUFFIX}"

PROD_RES=$(curl -s -X POST "$BASE_URL/catalog/products" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"$PROD_NAME\",
        \"slug\": \"$PROD_SLUG\",
        \"sku\": \"$PROD_SKU\",
        \"price\": 1000,
        \"categoryId\": \"$CAT_ID\",
        \"storeId\": \"$STORE_ID\",
        \"attributes\": {\"color\": \"red\", \"test_run\": \"$UNIQUE_SUFFIX\"}
    }")

PROD_ID=$(echo "$PROD_RES" | jq -r '.data.id // empty')

if [[ -n "$PROD_ID" ]]; then
    check_status "$(echo "$PROD_RES" | jq -r '.data.name')" "$PROD_NAME" "Création Produit"
else
    printf "${RED}[FAIL]${NC} Création Produit échouée\n"
    echo "$PROD_RES" | jq .
    exit 1
fi

# 3. Consultation Détails
printf "\n--- 🔍 Test Détails Produit ---\n"
# Utilisation du token pour éviter les refus PBAC si la ressource est protégée
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/catalog/products/$PROD_ID")
check_status "$STATUS" "200" "Consultation détails produit"

printf "\n✅ Tests du catalogue terminés avec succès (Run: $UNIQUE_SUFFIX) !\n"