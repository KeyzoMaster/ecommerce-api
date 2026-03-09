package com.lemzo.ecommerce.core.api.exception;

import java.util.Arrays;
import java.util.Optional;

/**
 * Exception métier lancée lorsqu'une règle métier est violée.
 */
public class BusinessRuleException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public BusinessRuleException(final String messageKey, final Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = Optional.ofNullable(args)
                .map(a -> Arrays.copyOf(a, a.length))
                .orElse(new Object[0]);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }
}
