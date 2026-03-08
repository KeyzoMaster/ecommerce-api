package com.lemzo.ecommerce.security.infrastructure.pabc;

import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.security.api.pabc.AuthorizationService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Intercepteur pour le contrôle d'accès Hybrid PBAC avec support de l'Ownership.
 * Délègue la validation à l'AuthorizationService.
 */
@HasPermission(resource = ResourceType.PLATFORM, action = PbacAction.READ) 
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class SecurityInterceptor {

    private static final Logger LOGGER = Logger.getLogger(SecurityInterceptor.class.getName());

    @Context
    private SecurityContext securityContext;

    @Inject
    private AuthorizationService authorizationService;

    @AroundInvoke
    public Object checkPermission(InvocationContext context) throws Exception {
        var annotation = Optional.ofNullable(context.getMethod().getAnnotation(HasPermission.class))
                .or(() -> Optional.ofNullable(context.getTarget().getClass().getAnnotation(HasPermission.class)));

        if (annotation.isEmpty()) {
            return context.proceed();
        }

        ResourceType resource = annotation.get().resource();
        PbacAction action = annotation.get().action();
        
        // Extraction de l'ID cible si demandé
        UUID targetId = annotation.get().checkOwnership() ? 
                extractResourceId(context).orElse(null) : null;

        if (!authorizationService.isAuthorized(securityContext, resource, action, targetId)) {
            LOGGER.warning(String.format("Accès refusé pour %s sur %s", 
                    Optional.ofNullable(securityContext.getUserPrincipal()).map(p -> p.getName()).orElse("ANONYMOUS"),
                    resource.name()));
            throw new ForbiddenException("Accès refusé : permissions ou propriété manquantes.");
        }

        return context.proceed();
    }

    private Optional<UUID> extractResourceId(InvocationContext context) {
        var parameters = context.getMethod().getParameters();
        var args = context.getParameters();

        return IntStream.range(0, parameters.length)
                .filter(i -> parameters[i].isAnnotationPresent(PathParam.class))
                .mapToObj(i -> {
                    try {
                        return Optional.of(UUID.fromString(args[i].toString()));
                    } catch (Exception e) {
                        return Optional.<UUID>empty();
                    }
                })
                .flatMap(Optional::stream)
                .findFirst();
    }
}
