# 💼 Logique Métier

L'application est découpée en sous-domaines (Sub-Domains) respectant les principes du Domain-Driven Design (DDD).

## 1. Catalogue (`domain-catalog`)
*   Gère les `Category` et `Product`.
*   Supporte des attributs dynamiques stockés en `JSONB` (ex: `{"couleur": "rouge", "stockage": "256GB"}`).
*   Inclut la recherche multi-critères et le full-text search.

## 2. Ventes & Paniers (`domain-sales`)
*   Gère les `Cart` (stockés temporairement en base ou en Redis).
*   Gère les `Order` et `OrderItem`.
*   Lors de la création d'une commande (`placeOrder`), le service :
    1. Vérifie la disponibilité via l'`InventoryPort`.
    2. Applique d'éventuels codes promo via le `MarketingPort`.
    3. Décrémente les stocks (via `decreaseStock`).
    4. Initie le paiement via le `PaymentPort`.

## 3. Inventaire (`domain-inventory`)
*   Gère les entités `Stock`.
*   Protège contre les quantités négatives.
*   Maintien des seuils d'alerte (`lowStockThreshold`) pour les rapports d'analytics.
*   Interfaces `increaseStock` et `decreaseStock` pour une gestion fine et atomique des transactions (remplaçant une simple méthode update).

## 4. Marketing (`domain-marketing`)
*   Gestion des `Coupon` (pourcentage ou montant fixe).
*   Suivi du nombre d'utilisations (`usageCount`) et de la date d'expiration.
*   Gestion des promotions directes sur produits (`ProductPromotion`).

## 5. Expédition (`domain-shipping`)
*   Gestion des statuts de livraison (`Shipment`).

## 6. IAM (Identity and Access Management)
*   Enregistrement, authentification, rafraîchissement JWT.
*   Gestion des entités `User`, `Role`, `Permission`, et `Store`.
*   Un utilisateur "Propriétaire de Boutique" possède un contexte `STORE` isolé (géré via PBAC récursif).

## 7. Analytics (`domain-analytics`)
*   Tableau de bord pour les vendeurs (Revenus, Commandes en attente).
*   Génération de fichiers CSV pour les tendances de vente.
*   Utilisation de requêtes SQL natives complexes (`createNativeQuery`) pour contourner les limitations JPQL sur l'agrégation de données avancées (ex: DATE_TRUNC).
