package com.lemzo.ecommerce.core.api.security;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation pour le contrôle d'accès granulaire (PBAC).
 * Supporte maintenant la vérification de propriété (Ownership).
 */
@InterceptorBinding
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface HasPermission {
    ResourceType resource();
    PbacAction action();
    
    /**
     * Si vrai, vérifie que l'utilisateur est le propriétaire de la ressource.
     */
    boolean checkOwnership() default false;
}
