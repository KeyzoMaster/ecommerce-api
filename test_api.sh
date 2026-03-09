#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080/api/v1"
printf "🚀 Démarrage des tests de l'API E-Commerce sur $BASE_URL\n"

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Attente du démarrage de GlassFish
printf "${YELLOW}⏳ Attente du démarrage du serveur...${NC}\n"
until curl -s "$BASE_URL/catalog/products" > /dev/null; do
    printf "."
    sleep 2
done
printf "\n${GREEN}✅ Serveur prêt !${NC}\n"

# Fonction utilitaire pour vérifier le statut HTTP
check_status() {
    if [[ "$1" -ge 200 && "$1" -lt 300 ]]; then
        printf "${GREEN}[OK]${NC} $2 (Status: $1)\n"
    else
        printf "${RED}[FAIL]${NC} $2 (Status: $1)\n"
        # On affiche le corps de la réponse en cas d'erreur
        [ -f /tmp/api_res.json ] && cat /tmp/api_res.json
        exit 1
    fi
}

# 1. TEST CATALOGUE PUBLIC
printf "\n--- 📦 Test Catalogue Public ---\n"
STATUS=$(curl -s -o /tmp/api_res.json -w "%{http_code}" "$BASE_URL/catalog/products")
check_status "$STATUS" "Liste des produits (Public)"

PRODUCT_SLUG=$(cat /tmp/api_res.json | jq -r '.content[0].slug')
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/catalog/products/$PRODUCT_SLUG")
check_status "$STATUS" "Détails produit : $PRODUCT_SLUG (Public)"

# 2. LOGIN CLIENT
printf "\n--- 🔐 Authentification Client ---\n"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"identifier": "client", "password": "client123"}')

CLIENT_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')

if [[ "$CLIENT_TOKEN" != "null" && "$CLIENT_TOKEN" != "" ]]; then
    printf "${GREEN}[OK]${NC} Login client réussi\n"
else
    printf "${RED}[FAIL]${NC} Login client échoué\n"
    echo $LOGIN_RESPONSE
    exit 1
fi

# 3. TEST PROFIL ET ADRESSES (PROTECTED)
printf "\n--- 👤 Test Profil & Adresses ---\n"
STATUS=$(curl -s -o /tmp/api_res.json -w "%{http_code}" -H "Authorization: Bearer $CLIENT_TOKEN" "$BASE_URL/users/me")
check_status "$STATUS" "Récupération profil (Protégé)"

# Ajouter une adresse
printf "Ajout d'une adresse...\n"
ADD_ADDR=$(curl -s -X POST "$BASE_URL/users/me/addresses" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"label": "Maison", "street": "Rue 10", "city": "Dakar", "country": "Sénégal"}')
    
ADDR_ID=$(echo $ADD_ADDR | jq -r '.data.addresses[0].id')
if [[ "$ADDR_ID" != "null" ]]; then
    printf "${GREEN}[OK]${NC} Adresse ajoutée (ID: $ADDR_ID)\n"
else
    printf "${RED}[FAIL]${NC} Échec ajout adresse\n"
    echo $ADD_ADDR
    exit 1
fi

# 4. TUNNEL D'ACHAT (PANIER & COMMANDE)
printf "\n--- 🛒 Test Panier & Commande ---\n"
PRODUCT_ID=$(curl -s "$BASE_URL/catalog/products" | jq -r '.content[0].id')

# Ajouter au panier
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/sales/cart/items" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"productId\": \"$PRODUCT_ID\", \"quantity\": 1}")
check_status "$STATUS" "Ajout au panier"

# Passer commande
printf "Passage de commande...\n"
ORDER_RES=$(curl -s -X POST "$BASE_URL/sales/orders" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"paymentProvider\": \"cod\",
        \"shippingMethod\": \"STANDARD\",
        \"items\": [{\"productId\": \"$PRODUCT_ID\", \"quantity\": 1}],
        \"shippingAddress\": {\"label\": \"Maison\", \"street\": \"Rue 10\", \"city\": \"Dakar\", \"country\": \"Sénégal\"}
    }")
    
ORDER_NUM=$(echo $ORDER_RES | jq -r '.data.orderNumber')
if [[ "$ORDER_NUM" != "null" && "$ORDER_NUM" != "" ]]; then
    printf "${GREEN}[OK]${NC} Commande passée (N°: $ORDER_NUM)\n"
else
    printf "${RED}[FAIL]${NC} Échec passage commande\n"
    echo $ORDER_RES
    exit 1
fi

# 5. TEST ANALYTICS & BOUTIQUE (ROLE OWNER)
printf "\n--- 🏪 Test Store Owner & Analytics ---\n"
OWNER_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"identifier": "owner", "password": "owner123"}')
OWNER_TOKEN=$(echo $OWNER_LOGIN | jq -r '.accessToken')

STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $OWNER_TOKEN" "$BASE_URL/analytics/dashboard")
check_status "$STATUS" "Accès Dashboard Analytics (Owner)"

# 6. TEST RÉVOCATION (LOGOUT)
printf "\n--- 🚪 Test Logout & Révocation ---\n"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/auth/logout" \
    -H "Authorization: Bearer $CLIENT_TOKEN")
check_status "$STATUS" "Logout (Révocation du token)"

# Vérifier que le token ne marche plus
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $CLIENT_TOKEN" "$BASE_URL/users/me")
if [[ "$STATUS" == "401" ]]; then
    printf "${GREEN}[OK]${NC} Accès refusé après logout (Vérification Révocation)\n"
else
    printf "${RED}[FAIL]${NC} Le token fonctionne encore après logout ! (Status: $STATUS)\n"
fi

printf "\n✅ Fin des tests. Tout semble fonctionner correctement.\n"
