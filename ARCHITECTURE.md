# 🏛️ Architecture du Projet

Ce projet implémente un **Monolithe Modulaire** stricte afin d'assurer une évolutivité maximale tout en gardant la simplicité de déploiement d'une application unique.

## 1. Séparation des Préoccupations (SoC)
Chaque domaine (`domain-catalog`, `domain-sales`, etc.) est isolé. Les dépendances entre domaines se font **uniquement** à travers des interfaces / contrats appelés "Ports" (ex: `InventoryPort`, `CatalogPort`) situés dans `domain-core` ou `core`.
L'objectif est d'empêcher un domaine A de requêter directement le Repository d'un domaine B.

## 2. Accès aux Données (Jakarta Data)
L'accès à PostgreSQL 18 est modernisé grâce à l'API **Jakarta Data 1.0**.
*   Utilisation de `BasicRepository` et des annotations `@Insert`, `@Update`, `@Delete`.
*   Support des fonctionnalités avancées de PostgreSQL : requêtes natives, `JSONB` pour les attributs flexibles.
*   Pagination native gérée via `PageRequest` et `Page`.

## 3. Sécurité : Hybrid PBAC vs RBAC Classique
Le contrôle d'accès dépasse le simple RBAC (Role-Based Access Control). Nous utilisons un système **Hybrid PBAC** (Policy-Based Access Control) couplé à des intercepteurs CDI.

### Pourquoi le PBAC au lieu du RBAC ?
Dans un RBAC classique, un utilisateur a un rôle (ex: "Vendeur") qui lui donne des droits globaux. Dans un contexte multi-boutiques, cela pose problème : un vendeur ne doit pouvoir modifier **que** les produits de sa propre boutique. 
Le **PBAC** permet de lier la permission à la **ressource elle-même** et au **contexte**.
*   **Granularité fine :** On vérifie l'action, le type de ressource et la propriété (`checkOwnership = true`).
*   **Annotation :** `@HasPermission(resource = ResourceType.ORDER, action = PbacAction.READ, checkOwnership = true)`
*   **Hiérarchie et Récursivité :** Les ressources ont des relations parent/enfant (Ex: `ORDER` -> `SALES` -> `STORE` -> `PLATFORM`). Les fournisseurs d'appartenance (`OwnershipProvider`) vérifient de manière récursive si l'utilisateur est propriétaire de la ressource ou d'une ressource parente (ex: s'il possède la boutique, il possède la commande).

## 4. HATEOAS et Synergie avec le PBAC
L'API est conçue pour être véritablement RESTful (Niveau 3 du modèle de maturité de Richardson) grâce à **HATEOAS** (Hypermedia As The Engine Of Application State).

### Qu'est-ce que HATEOAS ?
Au lieu de renvoyer uniquement des données brutes, l'API renvoie également des **liens de navigation** (`_links`) dictant les actions possibles à un instant T (ex: un lien vers le paiement si la commande est `PENDING`).
**Avantages par rapport à une API classique :**
*   **Découvrabilité :** Le client frontend n'a pas besoin de coder en dur les URLs ou la logique d'état ; il suit simplement les liens fournis.
*   **Évolution sans casse :** L'API peut changer ses URLs internes sans casser les clients, tant que les "relations" (ex: `rel: "cancel"`) restent les mêmes.

### Synergie PBAC + HATEOAS
Dans notre architecture, HATEOAS est couplé au moteur PBAC de manière transparente. 
Lors de la sérialisation d'une ressource (via notre `HateoasMapper`), le système évalue silencieusement les permissions de l'utilisateur connecté via l'`AuthorizationService`. 
* Si l'utilisateur n'a pas le droit d'exécuter une action (ex: il n'a pas la permission `ORDER:DELETE` ou n'est pas le propriétaire), **le lien d'action n'est tout simplement pas généré dans le JSON de réponse**.
* Le client frontend adapte ainsi dynamiquement son interface (ex: cacher le bouton "Supprimer") en se basant uniquement sur la présence ou non des liens HATEOAS.

## 5. Gestion des Exceptions
Un `ExceptionMapper` JAX-RS centralisé capture les `BusinessRuleException` et `ResourceNotFoundException` pour retourner des réponses JSON formatées et standardisées de type Problem Details.
