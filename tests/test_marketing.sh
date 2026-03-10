#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}🎁 TESTS MARKETING (COUPONS & PROMOS)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# 1. Création de Coupon (Admin)
printf "\n--- 🎟️  Test Création Coupon ---\n"
CODE="TEST-$(date +%s)"
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
check_status "$(echo "$COUPON_RES" | jq -r '.data.code')" "$CODE" "Création Coupon"

# 2. Validation de Coupon
printf "\n--- ✅ Test Validation Coupon ---\n"
VALID_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/marketing/coupons/$CODE")
check_status "$VALID_STATUS" "200" "Coupon valide"

# 3. Test Coupon inexistant
INVALID_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/marketing/coupons/EXPIRED")
check_status "$INVALID_STATUS" "404" "Coupon invalide/inexistant"

printf "\n✅ Fin des tests marketing.\n"
