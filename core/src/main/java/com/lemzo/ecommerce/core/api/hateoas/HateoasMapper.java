package com.lemzo.ecommerce.core.api.hateoas;

import com.lemzo.ecommerce.core.api.dto.Link;
import com.lemzo.ecommerce.core.api.dto.RestResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Mapper utilitaire pour construire des ressources HATEOAS.
 */
@ApplicationScoped
public class HateoasMapper {

    public <T> RestResponse<T> toResource(final T data, final UriInfo uriInfo) {
        final List<Link> links = List.of(
                Link.self(uriInfo.getAbsolutePath().toString())
        );
        return RestResponse.create(data, links);
    }

    public <T> RestResponse<T> toResource(final T data, final List<Link> links) {
        return RestResponse.create(data, List.copyOf(links));
    }
}
