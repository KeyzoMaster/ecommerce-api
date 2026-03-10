#!/bin/bash

# S'assurer d'être à la racine du projet
# cd "$(dirname "$0")/.."

printf "\033[1;34m🚀 DÉMARRAGE DE LA SUITE DE TESTS EXHAUSTIVE\033[0m\n"

# Rendre les scripts exécutables
chmod +x *.sh

BASE_URL="http://localhost:8080/api/v1"
printf "\033[1;33m⏳ Attente du démarrage du serveur...\033[0m\n"
until curl -s "$BASE_URL/catalog/products" > /dev/null; do
    printf "."
    sleep 2
done
printf "\n\033[0;32m✅ Serveur prêt !\033[0m\n"

# Exécution séquentielle
bash setup_users.sh || exit 1
bash test_security.sh || exit 1
bash test_iam.sh || exit 1
bash test_catalog.sh || exit 1
bash test_sales.sh || exit 1
bash test_marketing.sh || exit 1
bash test_analytics.sh || exit 1
bash test_storage.sh || exit 1
bash test_audit.sh || exit 1

printf "\n\033[1;32m🏆 TOUS LES MODULES DU DOMAINE ONT ÉTÉ VALIDÉS AVEC SUCCÈS !\033[0m\n"
