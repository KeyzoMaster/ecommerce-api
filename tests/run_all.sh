#!/bin/bash

# S'assurer d'être à la racine du projet
# cd "$(dirname "$0")/.."

printf "\033[1;34m🚀 DÉMARRAGE DE LA SUITE DE TESTS EXHAUSTIVE\033[0m\n"

# Rendre les scripts exécutables
chmod +x *.sh

# Exécution séquentielle
bash test_security.sh || exit 1
bash test_iam.sh || exit 1
bash test_catalog.sh || exit 1
bash test_sales.sh || exit 1
bash test_marketing.sh || exit 1
bash test_analytics.sh || exit 1
bash test_storage.sh || exit 1
bash test_audit.sh || exit 1

printf "\n\033[1;32m🏆 TOUS LES MODULES DU DOMAINE ONT ÉTÉ VALIDÉS AVEC SUCCÈS !\033[0m\n"
