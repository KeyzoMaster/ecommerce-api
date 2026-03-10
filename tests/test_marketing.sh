#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}🎁 TESTS MARKETING (COUPONS & PROMOS)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# 1. Création de Coupon (Admin)
printf "\n--- 🎟️  Test Création Coupon ---\n"
# Augmentation de l'unicité avec Nanosecondes + Nombre Aléatoire
CODE="TEST-$(date +%s%N)-${RANDOM}"

COUPON_RES=$(curl -s -X POST "$BASE_URL/marketing/coupons" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"code\": \"$CODE\",
        \"type\": \"PERCENTAGE\",
        \"value\": 10.0,
        \"startDate\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",
        \"endDate\": \"2026-12-31T23:59:59Z\"
    }")

# Extraction du code pour vérification
CREATED_CODE=$(echo "$COUPON_RES" | jq -r '.data.code // empty')

if [[ "$CREATED_CODE" == "$CODE" ]]; then
    check_status "$CREATED_CODE" "$CODE" "Création Coupon"
else
    printf "${RED}[FAIL]${NC} Création Coupon échouée (Code attendu: $CODE)\n"
    echo "$COUPON_RES" | jq .
    exit 1
fi

# 2. Validation de Coupon
printf "\n--- ✅ Test Validation Coupon ---\n"
VALID_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/marketing/coupons/$CODE")
check_status "$VALID_STATUS" "200" "Coupon valide"

# 3. Test Coupon inexistant
printf "\n--- ❌ Test Coupon Invalide ---\n"
INVALID_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/marketing/coupons/INVALID-CODE")
check_status "$INVALID_STATUS" "404" "Coupon inexistant"

printf "\n✅ Tests marketing terminés avec succès !\n"