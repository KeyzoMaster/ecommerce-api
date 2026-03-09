package com.lemzo.ecommerce.core.api.dto;

/**
 * Représentation d'un lien HATEOAS.
 */
public record Link(String rel, String href, String method) {
    public static Link self(final String href) {
        return new Link("self", href, "GET");
    }

    public static Link create(final String rel, final String href, final String method) {
        return new Link(rel, href, method);
    }
}
