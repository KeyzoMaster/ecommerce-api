# 🛒 E-Commerce API (Modular Monolith)

Bienvenue dans l'API E-Commerce de niveau professionnel. Ce projet est un **Monolithe Modulaire** conçu pour une plateforme multi-boutiques (Single Database), mettant en œuvre des pratiques d'ingénierie avancées.

## 🚀 Stack Technique
*   **Langage :** Java 25 (Records, Pattern Matching, Switch Expressions)
*   **Framework :** Jakarta EE 11 (JAX-RS, CDI)
*   **Serveur d'application :** GlassFish 8.0.0
*   **Bases de données :**
    *   PostgreSQL 18 (Données transactionnelles, JSONB natif)
    *   MongoDB (Logs d'audit et tracking)
*   **Cache & Session :** Redis (Mécanismes de mise en cache, révocation JWT)
*   **Stockage :** MinIO (S3-compatible) pour la gestion des images et fichiers
*   **ORM / Data Access :** Jakarta Data 1.0 (Repositories), Hibernate / EclipseLink
*   **Documentation :** MicroProfile OpenAPI 4.0 (Swagger UI)

## 📁 Structure du Projet
Le projet est divisé en plusieurs modules Maven avec des graphes de dépendance stricts :
*   `core` : Contrats, Entités de base, Exceptions communes.
*   `util` : Utilitaires transverses.
*   `storage` : Abstraction MinIO et configuration Redis.
*   `audit` : Intercepteurs CDI pour les logs MongoDB.
*   `security` : Authentification JWT, Intercepteurs Hybrid PBAC.
*   `iam` : Utilisateurs, Rôles, Boutiques.
*   `payment` : Ports et intégrations de paiement.
*   `domain` : Cœur de la logique métier (catalog, sales, inventory, marketing, analytics, shipping).
*   `api` : Point d'entrée JAX-RS, agrégation des modules, Packaging WAR.

## 🛠️ Démarrage Rapide
Consultez le fichier `DOCKER_SETUP.md` pour les instructions complètes de déploiement via Docker Compose.

## 📚 Documentation
- [Architecture du projet](ARCHITECTURE.md)
- [Logique Métier et Domaines](BUSINESS_LOGIC.md)
- [Guide de Déploiement Docker](DOCKER_SETUP.md)
