# 🐳 Déploiement et Lancement Docker

L'environnement de développement et de production est entièrement conteneurisé.

## Prérequis
*   Docker et Docker Compose installés.
*   Java 25 et Maven (pour la compilation).

## 1. Compiler le projet
Avant de lancer Docker, générez le livrable WAR.
```bash
mvn clean install -DskipTests
```
Le fichier généré se trouvera dans `api/target/api-1.0-SNAPSHOT.war` (et sera renommé implicitement par le `Dockerfile`).

## 2. Démarrer les services
À la racine du projet, lancez la commande suivante :
```bash
docker-compose up -d --build
```

### Services déployés :
*   **ecommerce_api** : Serveur d'application GlassFish 8.0.0 (Port 8080 pour HTTP, 4848 pour l'administration).
*   **ecommerce_postgres** : Base de données relationnelle PostgreSQL 18. Contient la base `ecommerce_db`. Les données initiales sont auto-injectées grâce au volume monté sur `/docker-entrypoint-initdb.d/`.
*   **ecommerce_mongo** : MongoDB pour le stockage asynchrone des logs d'audit générés par le système.
*   **ecommerce_redis** : Base in-memory Redis (Port 6379) pour la gestion des sessions utilisateurs et le stockage des tokens JWT révoqués (Rate Limiting et Blacklist).
*   **ecommerce_minio** : Stockage d'objets compatible S3 (Port 9000 pour l'API, 9001 pour la console d'administration web). Gère les images de produits et avatars.

## 3. Accès aux ressources
*   **API Principale :** `http://localhost:8080/api/v1`
*   **Swagger UI (Documentation Interactive) :** `http://localhost:8080/api/swagger-ui.html`
*   **MinIO Console :** `http://localhost:9001` (Identifiants par défaut : `admin` / `admin123`)
*   **GlassFish Admin Console :** `http://localhost:4848`

## 4. Tests bout-en-bout
Une fois tous les conteneurs démarrés et l'API fonctionnelle, vous pouvez valider le bon fonctionnement global en exécutant la suite de tests bash :
```bash
./test_api.sh
```
Ce script validera l'authentification, le routage PBAC, la création de produits et le processus de commande complet.
