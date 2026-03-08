# Étape 1 : Build de l'application avec Maven et Java 25
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY core/pom.xml ./core/
COPY util/pom.xml ./util/
COPY storage/pom.xml ./storage/
COPY audit/pom.xml ./audit/
COPY security/pom.xml ./security/
COPY iam/pom.xml ./iam/
COPY payment/pom.xml ./payment/
COPY domain/pom.xml ./domain/
COPY domain/domain-catalog/pom.xml ./domain/domain-catalog/
COPY domain/domain-sales/pom.xml ./domain/domain-sales/
COPY domain/domain-inventory/pom.xml ./domain/domain-inventory/
COPY domain/domain-marketing/pom.xml ./domain/domain-marketing/
COPY domain/domain-analytics/pom.xml ./domain/domain-analytics/
COPY domain/domain-shipping/pom.xml ./domain/domain-shipping/
COPY api/pom.xml ./api/

# Copier tous les répertoires sources
COPY core/src ./core/src
COPY util/src ./util/src
COPY storage/src ./storage/src
COPY audit/src ./audit/src
COPY security/src ./security/src
COPY iam/src ./iam/src
COPY payment/src ./payment/src
COPY domain/domain-catalog/src ./domain/domain-catalog/src
COPY domain/domain-sales/src ./domain/domain-sales/src
COPY domain/domain-inventory/src ./domain/domain-inventory/src
COPY domain/domain-marketing/src ./domain/domain-marketing/src
COPY domain/domain-analytics/src ./domain/domain-analytics/src
COPY domain/domain-shipping/src ./domain/domain-shipping/src
COPY api/src ./api/src

# Compiler le projet complet
RUN mvn clean package -DskipTests

# Étape 2 : Préparation de l'image d'exécution avec Java 25
FROM eclipse-temurin:25-jdk

# Installer wget et unzip
RUN apt-get update && apt-get install -y wget unzip && rm -rf /var/lib/apt/lists/*

# Télécharger et installer GlassFish 8.0.0
RUN wget https://download.eclipse.org/ee4j/glassfish/glassfish-8.0.0.zip -O /tmp/glassfish.zip && \
    unzip /tmp/glassfish.zip -d /opt && \
    rm /tmp/glassfish.zip

# Télécharger le driver JDBC PostgreSQL 42.7.x
RUN wget https://jdbc.postgresql.org/download/postgresql-42.7.5.jar -O /opt/glassfish8/glassfish/domains/domain1/lib/postgresql-42.7.5.jar

# Copier le .war généré depuis le module api
COPY --from=build /app/api/target/api.war /opt/glassfish8/glassfish/domains/domain1/autodeploy/api.war

# Exposer le port HTTP standard et le port admin
EXPOSE 8080 4848

# Démarrer GlassFish en mode verbeux
CMD ["/opt/glassfish8/bin/asadmin", "start-domain", "-v"]
