package com.lemzo.ecommerce.core.api.exception;

import java.util.Arrays;

/**
 * Exception métier lancée lorsqu'une règle métier est violée.
 */
public class BusinessRuleException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public BusinessRuleException(final String messageKey, final Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args != null ? Arrays.copyOf(args, args.length) : new Object[0];
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }
}
