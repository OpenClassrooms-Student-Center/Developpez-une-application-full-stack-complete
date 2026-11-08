package com.orion.mdd.common.exception;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Turns the errors raised by the services and by Spring MVC into Problem Details. It is the only place that gives
 * the errors of the services an HTTP status.
 */
@NullMarked
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

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

    /**
     * Answers 401 when the account of a token no longer exists: its owner counts as logged out.
     *
     * @return the Problem Details
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Vous devez être connecté.");
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Valeur invalide."),
                        (first, _) -> first));

        ProblemDetail problem = exception.getBody();
        problem.setDetail("Certaines données sont invalides.");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

}
