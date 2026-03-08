# 🚀 Feuille de Route : Finalisation API E-Commerce (M1)

Ce document récapitule l'état d'avancement du projet par rapport aux exigences du sujet d'examen et définit les tâches restantes pour garantir la note maximale.

## ✅ État d'avancement actuel
- **Infrastructure :** PostgreSQL 18 (UUID v7, Virtual Columns), MongoDB (Audit), Redis (Panier), MinIO (Storage), Flyway (Migrations).
- **Sécurité :** JWT (Access/Refresh), PBAC Hybride hiérarchique, Contrôle d'Ownership par annotation.
- **RESTful :** Niveau 3 Richardson (HATEOAS dynamique), Swagger complet en français.
- **Modules de base :** IAM, Catalog, Sales, Inventory, Marketing, Payment (Mocks), Analytics, Shipping.
- **Qualité :** JUnit 6, Mockito, JaCoCo (configuré pour Java 25), Zéro Null Check (Optional).

---

## 🛠 Tâches Restantes (Conformité PDF)

### 1. Module IAM : Profil et Paiement (Point 6)
- [x] Ajouter les champs `firstName` et `lastName` à l'entité `User`.
- [x] Créer l'endpoint `PUT /iam/users/me` pour la mise à jour du profil.
- [x] Implémenter la gestion des informations de paiement mockées (ex: stocker un token de carte fictif en JSONB).
- [x] Finaliser le CRUD des adresses.

### 2. Module Catalog : Recherche Avancée (Point 2)
- [x] Implémenter la **recherche textuelle (Full-Text Search)** via PostgreSQL 18.
- [x] Ajouter les filtres multi-critères : prix (min/max), catégorie, disponibilité.
- [x] Intégrer l'upload d'images dans le flux de création/mise à jour de produit.

### 3. Module Inventory : Alertes Stock (Point 7)
- [x] Implémenter un service de notification (Logs + Event) lorsque le seuil `low_stock_threshold` est atteint.
- [x] Créer un endpoint `GET /inventory/alerts` pour lister les produits en rupture ou stock bas.

### 4. Module Analytics : Business Intelligence (Point 9)
- [x] Implémenter le calcul du **taux de conversion** (via `view_count`).
- [x] Créer un endpoint d'**export CSV** pour le chiffre d'affaires et le top produits (via `CsvExportUtil`).

### 5. Module Sales & Shipping : Checkout complet (Point 3 & 5)
- [x] Permettre le choix du mode de livraison (`shippingMethod`) lors du passage de commande.
- [x] Intégrer le calcul des frais de port (dynamique : poids, config vendeur, montant total).

---

## 🏁 Phase Finale
- [ ] Atteindre les **30% de couverture de code** globale via JaCoCo.
- [ ] Générer le package final `Nom_Prenom_M1_API_Ecommerce.zip`.
- [ ] Vérifier la présence du guide de déploiement (Dockerfile + README).

**Deadline : Mercredi 10 mars 2026 à 18h.**
