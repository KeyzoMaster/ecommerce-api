# Étape 1 : Build de l'application avec Maven et Java 25
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copier le POM parent
COPY pom.xml .

# Copier tous les répertoires des modules (nécessaire pour l'installation locale)
COPY core ./core
COPY util ./util
COPY storage ./storage
COPY audit ./audit
COPY security ./security
COPY iam ./iam
COPY payment ./payment
COPY domain ./domain
COPY api ./api

# Installer les modules dans le dépôt local du container et générer le WAR final
# On utilise -e pour voir les erreurs détaillées si ça échoue
RUN mvn clean install -DskipTests -e -T 1C

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

# Copier le .war généré depuis le module api (après mvn install, il est dans target)
COPY --from=build /app/api/target/api.war /opt/glassfish8/glassfish/domains/domain1/autodeploy/api.war

# Exposer le port HTTP standard et le port admin
EXPOSE 8080 4848

# Démarrer GlassFish en mode verbeux
CMD ["/opt/glassfish8/bin/asadmin", "start-domain", "-v"]
