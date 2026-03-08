package com.lemzo.ecommerce.core.api.hateoas;

import com.lemzo.ecommerce.core.api.dto.Link;
import com.lemzo.ecommerce.core.api.dto.RestResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire pour construire des réponses HATEOAS.
 */
@ApplicationScoped
public class HateoasMapper {

    /**
     * Enveloppe des données avec un lien "self" automatique.
     */
    public <T> RestResponse<T> toResource(T data, UriInfo uriInfo) {
        List<Link> links = new ArrayList<>();
        links.add(Link.self(uriInfo.getAbsolutePath().toString()));
        return RestResponse.of(data, links);
    }

    /**
     * Enveloppe des données avec plusieurs liens.
     */
    public <T> RestResponse<T> toResource(T data, List<Link> links) {
        return RestResponse.of(data, links);
    }
}
