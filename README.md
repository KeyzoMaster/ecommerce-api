# 🛒 E-Commerce API - Plateforme Multi-Boutiques

Une API RESTful professionnelle et robuste pour une plateforme e-commerce multi-boutiques. Construite avec **Jakarta EE 11**, cette application adopte une architecture en **Monolithe Modulaire** et respecte le niveau 3 du modèle de maturité de Richardson grâce à l'intégration de **HATEOAS**. Elle intègre un système de contrôle d'accès granulaire basé sur les permissions (**PBAC**).

---

## 🏗️ Structure du Code : Architecture en Monolithe Modulaire

Le projet est organisé sous forme de monolithe modulaire via Maven. Ce choix architectural permet d'allier la simplicité de déploiement d'un monolithe à la maintenabilité d'une architecture microservices en isolant strictement les contextes métiers (Bounded Contexts) :

* 📦 **`api/`** : Le point d'entrée de l'application (configuration JAX-RS, OpenAPI, filtres globaux, serveurs).
* ⚙️ **`core/`** : Le socle technique. Il contient les DTOs génériques, la gestion des exceptions, l'infrastructure HATEOAS, et les contrats (Ports).
* 👥 **`iam/`** : *Identity & Access Management*. Gère les Utilisateurs, les Rôles, les Permissions et l'entité Boutique (Store).
* 🏢 **`domain/`** : Contient les différents sous-domaines métiers isolés :
    * `domain-catalog` : Produits, Catégories, Recherche.
    * `domain-sales` : Commandes, Paniers, Processus de paiement.
    * `domain-inventory` : Gestion des stocks.
    * `domain-marketing` : Coupons de réduction, Promotions.
    * `domain-shipping` : Modes de livraison, Expéditions.
    * `domain-analytics` : Tableaux de bord, Statistiques de ventes.
* 🛡️ **`security/`**, **`storage/`**, **`payment/`**, **`audit/`** : Modules transverses fournissant les implémentations d'infrastructure (JWT, MinIO, Redis, MongoDB, Mock Paiement).

---

## 🗄️ Les Entités et le Modèle de Données

Le modèle de données s'appuie sur un modèle relationnel robuste propulsé par **PostgreSQL** (JPA / Hibernate) avec des optimisations comme l'utilisation du `JSONB` et la recherche Full-Text, complété par **MongoDB** pour les logs d'audit.

Les entités sont découpées par domaine :
* **IAM** :
    * `User` : Représente les clients, les vendeurs et les administrateurs.
    * `Role` & `Permission` : Pour le contrôle d'accès.
    * `Store` : Représente la boutique d'un vendeur.
* **Catalogue** :
    * `Product` & `Category` : Les articles mis en vente liés à un `Store`.
* **Ventes (Sales)** :
    * `Order` & `OrderItem` : Mémorise les ventes (snapshot des prix au moment de l'achat).
    * `Cart` & `CartItem` : Gestion des paniers clients.

---

## 🔐 PBAC (Permission-Based Access Control)

Plutôt que de se limiter à un système RBAC (Role-Based Access Control) classique où les règles sont codées en dur pour des rôles (ex: `if(user.isStoreOwner())`), cette API implémente un système **PBAC**.

### Pourquoi ce choix ?
Le PBAC offre une granularité et une flexibilité maximales. Les rôles (Admin, Store Owner, Customer) ne sont que des "conteneurs" de permissions (`PbacAction` et `ResourceType`).

### Fonctionnement
La sécurité est appliquée via l'annotation `@HasPermission` sur les endpoints.
Un `SecurityInterceptor` vérifie dynamiquement :
1.  Si l'utilisateur a la permission requise (ex: `UPDATE` sur `PRODUCT`).
2.  Si la notion d'**Ownership** est requise (ex: un propriétaire de boutique ne peut modifier que *ses* propres produits). Si le tag `checkOwnership = true` est présent, le système valide automatiquement l'appartenance de la ressource via l'`OwnershipProvider`.

---

## 🔗 Le choix d'une API HATEOAS

Cette API est dite **"découvrable"**. Elle adhère pleinement au principe **HATEOAS** (Hypermedia as the Engine of Application State).

### Pourquoi HATEOAS ?
1.  **Découplage Client/Serveur** : Les clients (Front-end Web, Mobile) n'ont plus besoin de coder en dur les URLs des actions. L'API fournit directement les liens d'actions possibles en fonction de l'état de la ressource et des droits de l'utilisateur.
2.  **Navigation fluide** : Les réponses paginées (`PagedRestResponse`) intègrent automatiquement les liens de navigation (page précédente, page suivante, première, dernière).

### Implémentation
Toutes les réponses de l'API sont encapsulées dans des objets `RestResponse<T>` qui exposent une collection de `Link` (`rel`, `href`, `method`). La logique d'enrichissement est centralisée par un `HateoasMapper` afin de garantir une standardisation des réponses.

Exemple de réponse HATEOAS pour une commande :
```json
{
  "data": {
    "orderId": "1234",
    "status": "PENDING"
  },
  "_links": [
    { "rel": "self", "href": "/v1/sales/orders/1234", "method": "GET" },
    { "rel": "cancel", "href": "/v1/sales/orders/1234/cancel", "method": "POST" }
  ]
}