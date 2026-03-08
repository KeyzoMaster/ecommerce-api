package com.lemzo.ecommerce.audit.infrastructure;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.audit.service.AuditService;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Optional;
import java.util.UUID;

/**
 * Intercepteur pour l'audit automatique.
 */
@Audit
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class AuditInterceptor {

    @Inject
    private AuditService auditService;

    @Context
    private SecurityContext securityContext;

    @AroundInvoke
    public Object auditMethod(InvocationContext context) throws Exception {
        var annotation = Optional.ofNullable(context.getMethod().getAnnotation(Audit.class))
                .or(() -> Optional.ofNullable(context.getTarget().getClass().getAnnotation(Audit.class)));

        String action = annotation.map(Audit::action)
                .filter(a -> !a.isBlank())
                .orElseGet(() -> context.getMethod().getName());
        
        String resourceType = context.getTarget().getClass().getSimpleName();

        UUID userId = Optional.ofNullable(securityContext)
                .map(SecurityContext::getUserPrincipal)
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast)
                .map(AuthenticatedUser::getUserId)
                .orElse(null); // Passé au service qui gère l'absence d'utilisateur

        // Enregistre l'audit avant l'exécution
        auditService.log(
                userId,
                action,
                resourceType,
                "-",
                "Audit automatique de la méthode " + context.getMethod().getName(),
                "0.0.0.0",
                "Jakarta EE Interceptor"
        );

        return context.proceed();
    }
}
