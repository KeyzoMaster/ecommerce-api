#!/bin/bash

source "$(dirname "$0")/common.sh"
init_tokens

printf "${BLUE}====================================================${NC}\n"
printf "${BLUE}👤 TESTS IAM (PROFIL, RÔLES & ADRESSES)${NC}\n"
printf "${BLUE}====================================================${NC}\n"

# 1. Profil "Me"
printf "\n--- 👤 Test Profil Personnel ---\n"
ME_RES=$(curl -s "$BASE_URL/iam/users/me" -H "Authorization: Bearer $CLIENT_TOKEN")
check_status "$(echo "$ME_RES" | jq -r '.data.username')" "client" "Récupération profil 'me'"

# 2. Gestion des adresses
printf "\n--- 🏠 Test Gestion Adresses ---\n"
ADDR_RES=$(curl -s -X POST "$BASE_URL/iam/users/me/addresses" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"label": "Bureau", "street": "Place de l Indépendance", "city": "Dakar", "country": "Sénégal"}')
ADDR_ID=$(echo "$ADDR_RES" | jq -r '.data.addresses | last | .technicalId')
check_status "$(echo "$ADDR_RES" | jq -r '.data.addresses | last | .label')" "Bureau" "Ajout d'adresse"

# Modification
UPD_ADDR_RES=$(curl -s -X PUT "$BASE_URL/iam/users/me/addresses/$ADDR_ID" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"label": "Bureau Modifié", "street": "Nouvelle Rue", "city": "Dakar", "country": "Sénégal"}')
check_status "$(echo "$UPD_ADDR_RES" | jq -r ".data.addresses[] | select(.technicalId == \"$ADDR_ID\") | .label")" "Bureau Modifié" "Modification d'adresse"

# 3. Rôles et Permissions (Admin)
printf "\n--- 🔐 Test Administration Rôles ---\n"
ROLES_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/iam/roles" -H "Authorization: Bearer $ADMIN_TOKEN")
check_status "$ROLES_STATUS" "200" "Liste des rôles (Admin)"

PERMS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/iam/roles/permissions" -H "Authorization: Bearer $ADMIN_TOKEN")
check_status "$PERMS_STATUS" "200" "Liste des permissions (Admin)"

printf "\n✅ Fin des tests IAM.\n"
