#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}📦 TESTS DU CATALOGUE (PRODUITS & CATÉGORIES)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# 1. Création de Catégorie
printf "\n--- 📂 Test Catégorie ---\n"
CAT_RES=$(curl -s -X POST "$BASE_URL/catalog/categories" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name": "Test Cat", "slug": "test-cat", "description": "Desc"}')
CAT_ID=$(echo "$CAT_RES" | jq -r '.data.id')
check_status "$(echo "$CAT_RES" | jq -r '.data.name')" "Test Cat" "Création Catégorie"

# 2. Création de Produit
printf "\n--- 🏷️  Test Produit ---\n"
PROD_RES=$(curl -s -X POST "$BASE_URL/catalog/products" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Produit Test\",
        \"slug\": \"prod-test-$(date +%s)\",
        \"sku\": \"SKU-$(date +%s)\",
        \"price\": 1000,
        \"categoryId\": \"$CAT_ID\",
        \"attributes\": {\"color\": \"red\"}
    }")
PROD_ID=$(echo "$PROD_RES" | jq -r '.data.id')
check_status "$(echo "$PROD_RES" | jq -r '.data.name')" "Produit Test" "Création Produit"

# 3. Consultation Détails
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/catalog/products/$PROD_ID")
check_status "$STATUS" "200" "Consultation Produit"

# 4. Recherche
printf "\n--- 🔍 Test Recherche ---\n"
SEARCH_RES=$(curl -s "$BASE_URL/catalog/products?q=Produit")
COUNT=$(echo "$SEARCH_RES" | jq '.data.content | length')
if [[ $COUNT -gt 0 ]]; then
    printf "${GREEN}[OK]${NC} Recherche fonctionnelle ($COUNT résultats)\n"
else
    printf "${RED}[FAIL]${NC} Recherche n'a retourné aucun résultat\n"
    exit 1
fi

printf "\n✅ Fin des tests catalogue.\n"
