package com.lemzo.ecommerce.security.infrastructure.pabc;

import com.lemzo.ecommerce.core.api.security.HasPermission;
import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.security.api.pabc.AuthorizationService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.security.Principal;
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
@Dependent
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SecurityInterceptor {

    private static final Logger LOGGER = Logger.getLogger(SecurityInterceptor.class.getName());

    private final AuthorizationService authorizationService;

    @Context
    private SecurityContext securityContext;

    @AroundInvoke
    public Object checkPermission(final InvocationContext context) throws Exception {
        final HasPermission methodAnnotation = context.getMethod().getAnnotation(HasPermission.class);
        final HasPermission classAnnotation = context.getMethod().getDeclaringClass().getAnnotation(HasPermission.class);
        
        final var annotation = Optional.ofNullable(methodAnnotation)
                .or(() -> Optional.ofNullable(classAnnotation));

        if (annotation.isEmpty()) {
            return context.proceed();
        }

        final var resource = annotation.get().resource();
        final var action = annotation.get().action();
        
        final var targetId = annotation.get().checkOwnership() ? 
                extractResourceId(context).orElse(null) : null;

        if (!authorizationService.isAuthorized(securityContext, resource, action, targetId)) {
            final var principalName = Optional.ofNullable(securityContext.getUserPrincipal())
                    .map(Principal::getName)
                    .orElse("ANONYMOUS");
            
            LOGGER.warning(() -> String.format("Accès refusé pour %s sur %s:%s", principalName, resource, action));
            throw new ForbiddenException("Accès refusé : permissions ou propriété manquantes.");
        }

        return context.proceed();
    }

    private Optional<UUID> extractResourceId(final InvocationContext context) {
        final var parameters = context.getMethod().getParameters();
        final var args = context.getParameters();

        return IntStream.range(0, parameters.length)
                .filter(i -> parameters[i].isAnnotationPresent(PathParam.class))
                .mapToObj(i -> {
                    try {
                        return Optional.of(UUID.fromString(args[i].toString()));
                    } catch (final Exception e) {
                        return Optional.<UUID>empty();
                    }
                })
                .flatMap(Optional::stream)
                .findFirst();
    }
}
