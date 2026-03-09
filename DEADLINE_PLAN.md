# Plan de Secours - Finalisation API E-Commerce

Ce document contient les instructions pour finaliser l'API si l'assistant atteint ses limites avant la résolution complète.

## 1. État Actuel
- **Schéma DB :** Restauré en `uuid` natif avec `uuidv7()`.
- **Seeding :** Déplacé en SQL (`V4__Initial_Data.sql`) pour éviter les bugs Java/CDI.
- **Repositories :** Refactorisés pour éviter `save()` (non supporté) au profit de `insert()` et `update()`.
- **Problème bloquant :** Une erreur 500 persiste sur `GET /catalog/products`, probablement liée à la compilation JPQL des fonctions personnalisées (`fts_match`, `fts_rank`).

## 2. Actions de dernier recours (Si ça ne marche toujours pas)

### A. Simplifier les Requêtes JPQL
Si les fonctions `FUNC('fts_match', ...)` continuent de provoquer des `JPQLException` :
1. Allez dans `ProductRepository.java`.
2. Remplacez le contenu de `@Query` par du JPQL standard (ex: `LIKE %:query%`).
3. Sacrifiez temporairement la recherche plein texte PostgreSQL 18 pour stabiliser l'API.

### B. Vérifier les Logs complets
Les logs Docker sont parfois tronqués. Pour voir l'erreur exacte :
```bash
docker exec -it ecommerce_api cat /opt/glassfish8/glassfish/domains/domain1/logs/server.log | grep -A 50 "Caused by"
```
Recherchez spécifiquement des erreurs de type `PSQLException` (mismatch de type UUID ou JSONB).

### C. Problème de Type UUID
Si PostgreSQL se plaint encore que `uuid` attendu mais `varchar` reçu :
1. Dans `V1__Initial_Schema.sql`, remplacez `uuid` par `varchar(36)` PARTOUT.
2. Dans le code Java, gardez les champs en `UUID` (JPA fera la conversion).

### D. Bypass Jakarta Data
Si une méthode de repository refuse de fonctionner (ex: `UnsupportedOperationException`) :
1. Créez une classe `ProductRepositoryImpl`.
2. Injectez l' `EntityManager`.
3. Implémentez la méthode manuellement avec `em.createQuery()` ou `em.createNativeQuery()`.

## 3. Commandes Utiles pour le Debug
- **Reset Complet :** 
  ```bash
  docker exec ecommerce_postgres psql -U e_user -d ecommerce_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;" && docker compose build api && docker compose up -d api
  ```
- **Vérifier les fonctions DB :**
  ```bash
  docker exec ecommerce_postgres psql -U e_user -d ecommerce_db -c "\df"
  ```
- **Vérifier les données :**
  ```bash
  docker exec ecommerce_postgres psql -U e_user -d ecommerce_db -c "SELECT * FROM catalog_products;"
  ```

---
*Dernière mise à jour : 9 Mars 2026*
