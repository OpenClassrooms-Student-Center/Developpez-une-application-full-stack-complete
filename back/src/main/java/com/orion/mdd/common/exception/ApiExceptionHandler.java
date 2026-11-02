package com.orion.mdd.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns the errors raised by the services into Problem Details. It is the only place that gives them an HTTP status.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Answers 409 when a value is already taken. The {@code errors} property names the field, like a validation
     * error, so that the front-end shows both under the field.
     *
     * @param exception
     *            the conflict
     * @return the Problem Details
     */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setProperty("errors", exception.getErrors());
        return problem;
    }

}
