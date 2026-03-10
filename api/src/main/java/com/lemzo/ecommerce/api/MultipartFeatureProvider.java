package com.lemzo.ecommerce.api;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * Active nativement le support Multipart (y compris EntityPart)
 * sans casser l'auto-découverte (CDI/PBAC) de GlassFish.
 */
@Provider
public class MultipartFeatureProvider implements Feature {
    @Override
    public boolean configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(MultiPartFeature.class)) {
            context.register(MultiPartFeature.class);
        }
        return true;
    }
}