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
        [ -f /tmp/api_res.json ] && cat /tmp/api_res.json
        exit 1
    fi
}

# 0. ENREGISTREMENT ET PROMOTION DES UTILISATEURS
printf "\n--- 👤 Création des comptes de test ---\n"

register_user() {
    printf "Inscription de $1...\n"
    STATUS=$(curl -s -X POST -o /tmp/api_res.json -w "%{http_code}" "$BASE_URL/auth/register" \
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
    docker exec ecommerce_postgres psql -U e_user -d ecommerce_db -c "SELECT promote_user('$1', '$2');" > /dev/null
    printf "${GREEN}[OK]${NC} $1 promu\n"
}

# Inscription des 3 types d'utilisateurs
register_user "admin" "admin@ecommerce.local" "admin123"
register_user "client" "client@ecommerce.local" "client123"
register_user "owner" "owner@ecommerce.local" "owner123"

# Promotion manuelle (Bypass limitations API)
promote_user "admin" "SUPER_ADMIN"
promote_user "client" "CLIENT"
promote_user "owner" "STORE_OWNER"

# 1. LOGIN CLIENT
printf "\n--- 🔐 Authentification Client ---\n"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"identifier": "client@ecommerce.local", "password": "client123"}')

CLIENT_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')

if [[ "$CLIENT_TOKEN" != "null" && "$CLIENT_TOKEN" != "" ]]; then
    printf "${GREEN}[OK]${NC} Login client réussi\n"
else
    printf "${RED}[FAIL]${NC} Login client échoué\n"
    echo $LOGIN_RESPONSE
    exit 1
fi

# 2. LOGIN ADMIN
printf "\n--- 🔐 Authentification Admin ---\n"
ADMIN_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"identifier": "admin@ecommerce.local", "password": "admin123"}')
ADMIN_TOKEN=$(echo $ADMIN_LOGIN | jq -r '.accessToken')
if [[ "$ADMIN_TOKEN" != "null" ]]; then printf "${GREEN}[OK]${NC} Login admin réussi\n"; fi

# 3. TEST CATALOGUE PUBLIC & RECHERCHE
printf "\n--- 📦 Test Catalogue & Recherche (PostgreSQL 18) ---\n"
STATUS=$(curl -s -o /tmp/api_res.json -w "%{http_code}" "$BASE_URL/catalog/products")
check_status "$STATUS" "Liste des produits (Public)"

# Test Full-Text Search (LIKE fallback)
printf "Test de la recherche...\n"
STATUS=$(curl -s -o /tmp/api_res.json -w "%{http_code}" "$BASE_URL/catalog/products?q=MacBook")
check_status "$STATUS" "Recherche MacBook"

PRODUCT_ID=$(cat /tmp/api_res.json | jq -r '.data.content[0].id')
PRODUCT_SLUG=$(cat /tmp/api_res.json | jq -r '.data.content[0].slug')

# 4. TEST MARKETING
printf "\n--- 🔍 Test Marketing & Coupons ---\n"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/marketing/coupons/MARS2026")
check_status "$STATUS" "Validation Coupon MARS2026"

# 5. TEST PROFIL & ADRESSES
printf "\n--- 👤 Test Profil & Adresses ---\n"
# Ajout d'une adresse pour le client
printf "Ajout d'une adresse pour le client...\n"
STATUS=$(curl -s -X POST -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"label": "Bureau", "street": "Place de l Indépendance", "city": "Dakar", "country": "Sénégal"}')
check_status "$STATUS" "Ajout adresse client"

# Récupérer l'ID de l'adresse
ADDR_ID=$(curl -s -H "Authorization: Bearer $CLIENT_TOKEN" "$BASE_URL/users/me" | jq -r '.data.addresses[0].technicalId')

# 6. TUNNEL D'ACHAT (PANIER & COMMANDE)
printf "\n--- 🛒 Test Panier & Commande ---\n"

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
        \"shippingAddressId\": \"$ADDR_ID\",
        \"couponCode\": \"MARS2026\"
    }")
    
ORDER_NUM=$(echo $ORDER_RES | jq -r '.data.orderNumber')
if [[ "$ORDER_NUM" != "null" && "$ORDER_NUM" != "" ]]; then
    printf "${GREEN}[OK]${NC} Commande passée (N°: $ORDER_NUM)\n"
else
    printf "${RED}[FAIL]${NC} Échec passage commande\n"
    echo $ORDER_RES
    exit 1
fi

# 7. TEST ANALYTICS (ROLE ADMIN)
printf "\n--- 🏪 Test Analytics ---\n"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/analytics/dashboard")
check_status "$STATUS" "Accès Dashboard Analytics (Admin)"

# 8. TEST RÉVOCATION (LOGOUT)
printf "\n--- 🚪 Test Logout ---\n"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/auth/logout" \
    -H "Authorization: Bearer $CLIENT_TOKEN")
check_status "$STATUS" "Logout réussi"

printf "\n✅ Tous les tests sont passés avec succès !\n"
