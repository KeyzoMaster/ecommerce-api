#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}📁 TESTS DE STOCKAGE (UPLOAD D'IMAGES)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# Créer un fichier de test
echo "fake image content" > /tmp/test_image.jpg

# 1. Upload Image Produit
printf "\n--- 🖼️  Test Upload Image Produit ---\n"
PRODUCT_ID="018e2345-6789-7000-c000-000000000001"
IMG_RES=$(curl -s -X POST "$BASE_URL/catalog/products/$PRODUCT_ID/image" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -F "file=@/tmp/test_image.jpg;type=image/jpeg")

IMG_PATH=$(echo "$IMG_RES" | jq -r '.imagePath // empty')
check_status "$(echo "$IMG_RES" | jq -r '.imagePath != null')" "true" "Upload Image Produit"

# 2. Vérification Accessibilité (Presigned URL)
printf "\n--- 🔗 Test URL Présignée ---\n"
PROD_DETAILS=$(curl -s "$BASE_URL/catalog/products/$PRODUCT_ID")
PRESIGNED_URL=$(echo "$PROD_DETAILS" | jq -r '.data.imageUrl')

if [[ "$PRESIGNED_URL" == http* ]]; then
    printf "${GREEN}[OK]${NC} URL présignée générée : ${PRESIGNED_URL:0:50}...\n"
    # Vérifier que l'URL répond (MinIO)
    IMG_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$PRESIGNED_URL")
    check_status "$IMG_STATUS" "200" "Accès à l'image stockée"
else
    printf "${RED}[FAIL]${NC} URL présignée invalide ou absente\n"
    exit 1
fi

# 3. Upload Avatar Utilisateur
printf "\n--- 👤 Test Upload Avatar Utilisateur ---\n"
USER_ID=$(curl -s -H "Authorization: Bearer $CLIENT_TOKEN" "$BASE_URL/iam/users/me" | jq -r '.data.id')
AVATAR_RES=$(curl -s -X POST "$BASE_URL/iam/users/$USER_ID/avatar" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -F "file=@/tmp/test_image.jpg;type=image/jpeg")
check_status "$(echo "$AVATAR_RES" | jq -r '.avatarPath != null')" "true" "Upload Avatar"

printf "\n✅ Fin des tests de stockage.\n"
