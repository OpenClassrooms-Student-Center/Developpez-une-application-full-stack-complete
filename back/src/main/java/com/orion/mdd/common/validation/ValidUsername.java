package com.orion.mdd.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A username: from 3 to 30 unaccented letters, digits, dots, underscores or hyphens. Each broken rule
 * reports its own message.
 */
@NotBlank(message = "Le nom d’utilisateur est obligatoire.")
@Pattern(regexp = "[A-Za-z0-9._-]{3,30}",
        message = "De 3 à 30 caractères : lettres sans accent, chiffres, « . », « _ » ou « - ».")
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidUsername {

    String message() default "Ce nom d’utilisateur n’est pas valide.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
