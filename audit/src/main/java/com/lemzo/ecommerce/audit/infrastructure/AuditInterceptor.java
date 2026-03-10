package com.lemzo.ecommerce.audit.infrastructure;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.audit.service.AuditService;
import com.lemzo.ecommerce.core.api.security.AuthenticatedUser;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Intercepteur pour l'audit automatique.
 */
@Audit
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@Dependent
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuditInterceptor {

    private final AuditService auditService;

    @Context
    private SecurityContext securityContext;

    @AroundInvoke
    public Object auditMethod(final InvocationContext context) throws Exception {
        final Optional<Audit> auditAnnotation = Optional.ofNullable(context.getMethod().getAnnotation(Audit.class))
                .or(() -> Optional.ofNullable(context.getTarget().getClass().getAnnotation(Audit.class)));

        final String auditAction = auditAnnotation.map(Audit::action)
                .filter(actionName -> !actionName.isBlank())
                .orElseGet(() -> context.getMethod().getName());

        final UUID authUserId = Optional.ofNullable(securityContext)
                .map(SecurityContext::getUserPrincipal)
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast)
                .map(AuthenticatedUser::getUserId)
                .orElse(null);

        final ResourceType auditResourceType = ResourceType.PLATFORM;

        auditService.log(
                authUserId,
                auditAction,
                auditResourceType,
                "-",
                Map.of("method", context.getMethod().getName(), "target", context.getTarget().getClass().getName()),
                "0.0.0.0",
                "Jakarta EE Interceptor"
        );

        return context.proceed();
    }
}
