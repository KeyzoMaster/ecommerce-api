#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}🕵️  TESTS D'AUDIT (MONGODB LOGGING)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# 1. Déclencher une action auditable (Mise à jour produit)
printf "\n--- ⚡ Déclenchement Action Auditable ---\n"
PRODUCT_ID="018e2345-6789-7000-c000-000000000001"
curl -s -X PUT "$BASE_URL/catalog/products/$PRODUCT_ID" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name": "MacBook Pro M3 Audité", "price": 1600000}' > /dev/null

printf "Attente de la persistance NoSQL...\n"
sleep 2

# 2. Vérifier la présence dans MongoDB
printf "\n--- 🔎 Vérification MongoDB ---\n"
# On cherche un document avec l'action CATALOG_UPDATE ou PRODUCT_UPDATE
AUDIT_COUNT=$(docker exec ecommerce_mongodb mongosh ecommerce_audit --quiet --eval "db.audits.countDocuments({ action: /.*PRODUCT.*/ })")

if [[ "$AUDIT_COUNT" -gt 0 ]]; then
    printf "${GREEN}[OK]${NC} Audit trouvé dans MongoDB ($AUDIT_COUNT entrées)\n"
else
    printf "${RED}[FAIL]${NC} Aucun log d'audit trouvé pour cette action\n"
    exit 1
fi

printf "\n✅ Fin des tests d'audit.\n"
