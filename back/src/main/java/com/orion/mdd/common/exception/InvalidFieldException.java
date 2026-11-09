package com.orion.mdd.common.exception;

import lombok.Getter;

import java.util.Map;

/**
 * A field is well formed but its value is refused, such as a wrong current password.
 */
@Getter
public class InvalidFieldException extends RuntimeException {

    private final Map<String, String> errors;

    /**
     * Names the refused field.
     *
     * @param field
     *            the name of the field
     * @param message
     *            the message for the user
     */
    public InvalidFieldException(String field, String message) {
        super(message);
        this.errors = Map.of(field, message);
    }

}
