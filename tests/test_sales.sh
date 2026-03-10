#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}🛒 TESTS DES VENTES & STOCKS (CART & ORDERS)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# 1. Sélection d'un produit (MacBook Pro du seeding)
PRODUCT_ID="018e2345-6789-7000-c000-000000000001"

# 2. Vérification Stock Initial
printf "\n--- 📉 Test Inventaire Initial ---\n"
STOCK_INIT=$(curl -s "$BASE_URL/inventory/$PRODUCT_ID" | jq -r '.data.quantity')
printf "Stock initial : $STOCK_INIT\n"

# 3. Gestion du Panier
printf "\n--- 🛍️  Test Panier ---\n"
curl -s -X DELETE "$BASE_URL/sales/cart" -H "Authorization: Bearer $CLIENT_TOKEN" > /dev/null
ADD_RES=$(curl -s -X POST "$BASE_URL/sales/cart/items?productId=$PRODUCT_ID&quantity=2" \
    -H "Authorization: Bearer $CLIENT_TOKEN")
check_status "$?" "0" "Ajout au panier"

# 4. Passage de Commande
printf "\n--- 🧾 Test Commande ---\n"
# Récupération d'une adresse client
ADDR_ID=$(curl -s -H "Authorization: Bearer $CLIENT_TOKEN" "$BASE_URL/iam/users/me" | jq -r '.data.addresses[0].technicalId // empty')
if [[ -z "$ADDR_ID" ]]; then
    printf "Ajout adresse de secours...\n"
    curl -s -X POST "$BASE_URL/iam/users/me/addresses" \
        -H "Authorization: Bearer $CLIENT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"label": "Test", "street": "Street", "city": "Dakar", "country": "Sénégal"}' > /dev/null
    ADDR_ID=$(curl -s -H "Authorization: Bearer $CLIENT_TOKEN" "$BASE_URL/iam/users/me" | jq -r '.data.addresses[0].technicalId')
fi

ORDER_RES=$(curl -s -X POST "$BASE_URL/sales/orders" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"shippingAddressId\": \"$ADDR_ID\",
        \"shippingMethod\": \"EXPRESS\",
        \"paymentProvider\": \"STRIPE\"
    }")
ORDER_NUM=$(echo "$ORDER_RES" | jq -r '.data.orderNumber')
ORDER_ID=$(echo "$ORDER_RES" | jq -r '.data.id')
check_status "$(echo "$ORDER_RES" | jq -r '.data.orderNumber != null')" "true" "Passage Commande (N° $ORDER_NUM)"

# 5. Vérification Stock Final (Doit avoir baissé de 2)
printf "\n--- 📈 Test Mise à jour Stock ---\n"
STOCK_FINAL=$(curl -s "$BASE_URL/inventory/$PRODUCT_ID" | jq -r '.data.quantity')
EXPECTED_STOCK=$((STOCK_INIT - 2))
check_status "$STOCK_FINAL" "$EXPECTED_STOCK" "Décrémentation auto du stock"

# 6. Test Expédition
printf "\n--- 🚚 Test Expédition ---\n"
# On simule le passage en SHIPPED pour déclencher la création du shipment
curl -s -X PATCH "$BASE_URL/sales/orders/$ORDER_ID/status?status=SHIPPED" \
    -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null

SHIP_RES=$(curl -s "$BASE_URL/shipping/order/$ORDER_ID" -H "Authorization: Bearer $CLIENT_TOKEN")
check_status "$(echo "$SHIP_RES" | jq -r '.data.trackingNumber != null')" "true" "Suivi expédition créé"

printf "\n✅ Fin des tests ventes et stocks.\n"
