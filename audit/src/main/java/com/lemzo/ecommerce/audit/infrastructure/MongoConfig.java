package com.lemzo.ecommerce.audit.infrastructure;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MongoConfig {

    @ConfigProperty(name = "MONGO_URL", defaultValue = "mongodb://localhost:27017")
    private String mongoUrl;

    @ConfigProperty(name = "MONGO_DATABASE", defaultValue = "ecommerce_audit")
    private String databaseName;

    @Produces
    @ApplicationScoped
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUrl);
    }

    @Produces
    @ApplicationScoped
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(databaseName);
    }
}
