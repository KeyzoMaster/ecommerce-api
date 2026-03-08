# 📊 Rapport d'Analyse et Feuille de Route de Finalisation

Ce document présente une analyse critique de la logique métier actuelle et définit les étapes nécessaires pour finaliser l'API conformément aux exigences de l'examen M1.

---

## 🔍 1. Analyse Critique de l'Existant

### 🛡️ Module IAM (Identité)
*   **Problème :** Les adresses sont stockées en liste simple sans identifiant unique ni label (Maison, Bureau). Cela rend le CRUD partiel (`removeAddress(index)`) fragile.
*   **Amélioration :** Transformer `Address` en entité liée ou ajouter un UUID technique et un label pour une gestion plus professionnelle.

### 📦 Module Catalog (Produits)
*   **Force :** Excellente utilisation de PostgreSQL 18 (FTS, JSONB, Trigrammes).
*   **Problème :** L'incrémentation des vues est synchrone. Bien que simple pour l'examen, elle pourrait être optimisée.
*   **Manque :** La validation du schéma JSON de `shippingConfig` n'est pas faite, ce qui peut causer des erreurs de calcul de frais de port si le vendeur saisit n'importe quoi.

### 🛒 Module Sales & Shipping (Ventes & Livraison)
*   **🚨 Erreur Critique :** La colonne virtuelle `total_price` en base de données ne contient que `shipping_cost`. Elle ignore le montant des articles car une colonne générée Postgres ne peut pas agréger les données d'une autre table (`order_items`).
*   **Correction :** Le prix total doit être calculé en Java dans le `SalesService` et stocké en colonne physique.
*   **Manque :** Absence d'interface pour le rôle `STORE_OWNER` pour suivre les commandes de sa boutique.

### 🎁 Module Marketing (Coupons)
*   **Problème :** Le module est isolé. Les coupons sont créables mais ne sont **jamais appliqués** lors du passage de commande.
*   **Amélioration :** Intégrer la validation et le calcul de remise dans le tunnel d'achat.

---

## 🛠 2. Simplification Stratégique (Objectif Examen)

Pour garantir la stabilité et la note maximale, nous allons :
1.  **Abandonner Cucumber** au profit de tests unitaires JUnit 6 massifs (plus simples à maintenir et à faire tourner en CI).
2.  **Centraliser les calculs en Java** plutôt qu'en base de données (plus facile à tester avec Mockito).
3.  **Utiliser des schémas JSON simples** pour les configurations vendeurs.

---

## 🚀 3. Feuille de Route Détaillée (Deadline : 10 Mars)

### Étape 1 : Correction et Intégration Sales/Marketing (Priorité 1)
- [x] **DB :** Modifier `sales_orders` pour passer `total_price` en colonne physique (non-générée).
- [x] **Marketing :** Ajouter une méthode `applyCoupon(String code, BigDecimal currentAmount)` dans `MarketingService`.
- [x] **Sales :** Mettre à jour `SalesService.placeOrder` pour :
    - Accepter un `couponCode` optionnel.
    - Appliquer la remise si le coupon est valide.
    - Calculer le `totalPrice` final (Articles + Port - Remise) et le persister.

### Étape 2 : Finalisation IAM et Adresses (Priorité 2)
- [x] **Core :** Ajouter un champ `label` à l'objet `Address`.
- [x] **IAM :** S'assurer que le paiement mocké stocke correctement le token en JSONB.

### Étape 3 : Interface Store Owner (Priorité 3)
- [x] **Sales :** Créer `GET /sales/orders/store/{storeId}` pour permettre aux vendeurs de voir leurs ventes.
- [x] **Security :** Vérifier que les permissions `@HasPermission` couvrent bien ces nouveaux accès.

### Étape 4 : Qualité et Couverture (Objectif 30%)
- [x] **Tests Unitaires :** Implémenter les suites de tests pour :
    - `CatalogService` (Recherche, CRUD).
    - `SalesService` (Tunnel d'achat complet avec calculs).
    - `UserService` (Profil, Adresses).
    - `MarketingService` (Validité des coupons).
- [ ] **JaCoCo :** Générer le rapport final et vérifier le seuil des 30%.

### Étape 5 : Packaging Final
- [ ] **Documentation :** Finaliser les Javadoc en Français et les descriptions OpenAPI.
- [ ] **Docker :** Vérifier que `docker-compose up` lance bien l'environnement complet (Postgres, Mongo, Redis, MinIO).
- [ ] **Archive :** Créer le zip `Nom_Prenom_M1_API_Ecommerce.zip`.

---
*Document généré le 08/03/2026 par Gemini CLI.*
