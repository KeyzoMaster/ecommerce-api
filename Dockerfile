# Étape 1 : Build de l'application avec Maven et Java 25
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copier le pom.xml et les sources
COPY pom.xml .
COPY src ./src

# Compiler le .war
RUN mvn clean package -DskipTests

# Étape 2 : Préparation de l'image d'exécution avec Java 25 pur
FROM eclipse-temurin:25-jdk

# Installer wget et unzip
RUN apt-get update && apt-get install -y wget unzip && rm -rf /var/lib/apt/lists/*

# Télécharger et installer GlassFish 8.0.0
RUN wget https://download.eclipse.org/ee4j/glassfish/glassfish-8.0.0.zip -O /tmp/glassfish.zip && \
    unzip /tmp/glassfish.zip -d /opt && \
    rm /tmp/glassfish.zip

# Télécharger le driver JDBC PostgreSQL
RUN wget https://jdbc.postgresql.org/download/postgresql-42.7.9.jar -O /opt/glassfish8/glassfish/domains/domain1/lib/postgresql-42.7.2.jar

# Copier le .war généré vers le dossier d'autodéploiement de GlassFish
COPY --from=build /app/target/ecommerce.war /opt/glassfish8/glassfish/domains/domain1/autodeploy/ecommerce.war

# Exposer le port HTTP
EXPOSE 8080

# Démarrer GlassFish
CMD ["/opt/glassfish8/bin/asadmin", "start-domain", "-v"]