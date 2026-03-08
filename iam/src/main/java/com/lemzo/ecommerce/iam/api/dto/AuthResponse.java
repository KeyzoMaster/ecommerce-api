package com.lemzo.ecommerce.iam.api.dto;

import com.lemzo.ecommerce.core.api.dto.Link;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;

@Schema(description = "Réponse suite à une authentification réussie")
public record AuthResponse(
    @Schema(description = "Jeton d'accès JWT")
    String accessToken,
    
    @Schema(description = "Jeton de rafraîchissement JWT")
    String refreshToken,
    
    @Schema(description = "Liens HATEOAS pour les actions suivantes")
    List<Link> links
) {}
