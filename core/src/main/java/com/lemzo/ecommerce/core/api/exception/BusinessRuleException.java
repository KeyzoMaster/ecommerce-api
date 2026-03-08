package com.lemzo.ecommerce.core.api.exception;

/**
 * Exception lancée lorsqu'une règle métier est violée.
 */
public class BusinessRuleException extends RuntimeException {
    private final String messageKey;
    private final Object[] args;

    public BusinessRuleException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
