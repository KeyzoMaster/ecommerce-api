#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}🛒 TESTS DES VENTES & STOCKS (CART & ORDERS)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# ===================================================================
# 0. SETUP : PRÉPARATION DES DONNÉES (Indépendant du Seeder)
# ===================================================================
printf "\n--- 🛠️  Préparation des données (Catégorie & Produit) ---\n"

# Création d'une catégorie de test
CAT_RES=$(curl -s -X POST "$BASE_URL/catalog/categories" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name": "Catégorie Sales", "slug": "cat-sales-'${RANDOM}'", "description": "Auto-générée pour les tests"}')
CAT_ID=$(echo "$CAT_RES" | jq -r '.data.id // empty')

# Identifiant fictif pour la boutique (Store)
STORE_ID="018e2345-6789-7000-7000-000000000001"

# Création d'un produit de test unique
PROD_RES=$(curl -s -X POST "$BASE_URL/catalog/products" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Produit Test Ventes\",
        \"slug\": \"prod-sales-${RANDOM}\",
        \"sku\": \"SKU-SALES-${RANDOM}\",
        \"price\": 15000,
        \"categoryId\": \"$CAT_ID\",
        \"storeId\": \"$STORE_ID\",
        \"attributes\": {}
    }")
PRODUCT_ID=$(echo "$PROD_RES" | jq -r '.data.id // empty')

if [[ -z "$PRODUCT_ID" || "$PRODUCT_ID" == "null" ]]; then
    printf "${RED}[FAIL]${NC} Impossible de créer le produit de test.\n"
    echo "$PROD_RES" | jq .
    exit 1
fi
printf "${GREEN}[OK]${NC} Produit de test créé : $PRODUCT_ID\n"

# Initialisation forcée du stock à 100 pour ce produit
curl -s -X PUT "$BASE_URL/inventory/$PRODUCT_ID" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"quantity": 100, "lowStockThreshold": 10}' > /dev/null


# ===================================================================
# 1. DÉBUT DES TESTS MÉTIERS
# ===================================================================

# Vérification Stock Initial
printf "\n--- 📉 Test Inventaire Initial ---\n"
STOCK_INIT_RES=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/inventory/$PRODUCT_ID")

if ! echo "$STOCK_INIT_RES" | jq . >/dev/null 2>&1; then
    printf "${RED}[FAIL]${NC} L'API a renvoyé une erreur non-JSON (500 probable).\n"
    echo "$STOCK_INIT_RES"
    exit 1
fi

STOCK_INIT=$(echo "$STOCK_INIT_RES" | jq -r '.data.quantity // empty')
if [[ -z "$STOCK_INIT" || "$STOCK_INIT" == "null" ]]; then
    printf "${RED}[FAIL]${NC} Impossible de récupérer le stock initial.\n"
    exit 1
fi
printf "Stock initial validé : $STOCK_INIT unités\n"

# Gestion du Panier (Rôle Client)
printf "\n--- 🛍️  Test Panier ---\n"
curl -s -X DELETE "$BASE_URL/sales/cart" -H "Authorization: Bearer $CLIENT_TOKEN" > /dev/null

ADD_RES_CODE=$(curl -s -X POST -o /dev/null -w "%{http_code}" \
    "$BASE_URL/sales/cart/items?productId=$PRODUCT_ID&quantity=2" \
    -H "Authorization: Bearer $CLIENT_TOKEN")
check_status "$ADD_RES_CODE" "200" "Ajout au panier"

# Passage de Commande
printf "\n--- 🧾 Test Commande ---\n"
ADDR_ID=$(curl -s -H "Authorization: Bearer $CLIENT_TOKEN" "$BASE_URL/iam/users/me" | jq -r '.data.addresses[0].id // empty')

if [[ -z "$ADDR_ID" || "$ADDR_ID" == "null" ]]; then
    printf "Ajout d'une adresse de secours...\n"
    curl -s -X POST "$BASE_URL/iam/users/me/addresses" \
        -H "Authorization: Bearer $CLIENT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"label": "Domicile", "street": "123 Rue de Test", "city": "Dakar", "country": "Sénégal"}' > /dev/null
    ADDR_ID=$(curl -s -H "Authorization: Bearer $CLIENT_TOKEN" "$BASE_URL/iam/users/me" | jq -r '.data.addresses[0].id')
fi

ORDER_RES=$(curl -s -X POST "$BASE_URL/sales/orders" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"shippingAddressId\": \"$ADDR_ID\",
        \"shippingMethod\": \"EXPRESS\",
        \"paymentProvider\": \"STRIPE\"
    }")

ORDER_NUM=$(echo "$ORDER_RES" | jq -r '.data.orderNumber // empty')
ORDER_ID=$(echo "$ORDER_RES" | jq -r '.data.id // empty')

if [[ -n "$ORDER_NUM" && "$ORDER_NUM" != "null" ]]; then
    check_status "201" "201" "Passage Commande (N° $ORDER_NUM)"
else
    printf "${RED}[FAIL]${NC} Échec de la commande\n"
    echo "$ORDER_RES" | jq .
    exit 1
fi

# Vérification Stock Final (Doit avoir baissé de 2)
printf "\n--- 📈 Test Mise à jour Stock ---\n"
STOCK_FINAL=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/inventory/$PRODUCT_ID" | jq -r '.data.quantity')
EXPECTED_STOCK=$((STOCK_INIT - 2))

if [[ "$STOCK_FINAL" == "$EXPECTED_STOCK" ]]; then
    printf "${GREEN}[OK]${NC} Décrémentation auto du stock (Obtenu: $STOCK_FINAL)\n"
else
    printf "${RED}[FAIL]${NC} Décrémentation auto du stock (Attendu: $EXPECTED_STOCK, Obtenu: $STOCK_FINAL)\n"
    exit 1
fi

# Test Expédition
printf "\n--- 🚚 Test Expédition ---\n"
STATUS_PATCH=$(curl -s -o /dev/null -w "%{http_code}" -X PATCH \
    "$BASE_URL/sales/orders/$ORDER_ID/status?status=SHIPPED" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
check_status "$STATUS_PATCH" "200" "Statut passé à SHIPPED"

SHIP_RES=$(curl -s "$BASE_URL/shipping/order/$ORDER_ID" -H "Authorization: Bearer $CLIENT_TOKEN")
TRACKING=$(echo "$SHIP_RES" | jq -r '.data.trackingNumber // empty')

if [[ -n "$TRACKING" && "$TRACKING" != "null" ]]; then
    printf "${GREEN}[OK]${NC} Suivi expédition créé ($TRACKING)\n"
else
    printf "${RED}[FAIL]${NC} Aucun numéro de suivi généré\n"
    echo "$SHIP_RES" | jq .
    exit 1
fi

printf "\n✅ Fin des tests ventes et stocks.\n"