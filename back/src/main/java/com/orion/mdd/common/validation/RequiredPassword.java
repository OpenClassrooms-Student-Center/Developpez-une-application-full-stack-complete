package com.orion.mdd.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A password that must be given. The login checks only this: the other rules apply when a password is chosen, so
 * that an account still opens if they change.
 */
@NotBlank(message = "Le mot de passe est obligatoire.")
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiredPassword {

    String message() default "Le mot de passe est obligatoire.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
