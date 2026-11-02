package com.orion.mdd.common.exception;

import lombok.Getter;

import java.util.Map;

/**
 * A value that must be unique is already taken.
 */
@Getter
public class ConflictException extends RuntimeException {

    private final Map<String, String> errors;

    /**
     * Names the field that holds the taken value.
     *
     * @param field
     *            the name of the field
     * @param message
     *            the message for the user
     */
    public ConflictException(String field, String message) {
        super(message);
        this.errors = Map.of(field, message);
    }

    /**
     * Reports a conflict that no single field explains.
     *
     * @param message
     *            the message for the user
     */
    public ConflictException(String message) {
        super(message);
        this.errors = Map.of();
    }

}
