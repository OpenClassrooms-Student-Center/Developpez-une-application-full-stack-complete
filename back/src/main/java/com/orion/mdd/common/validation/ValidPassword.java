package com.orion.mdd.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A password: from 8 to 64 characters, with a digit, a lowercase letter, an uppercase letter and a
 * character that is neither a letter nor a digit. Each broken rule reports its own message.
 */
@RequiredPassword
@Pattern(regexp = "(?=.*\\d)(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*[^\\p{L}\\p{N}]).{8,64}",
        message = "De 8 à 64 caractères, avec un chiffre, une minuscule, une majuscule et un caractère spécial.")
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidPassword {

    String message() default "Ce mot de passe n’est pas valide.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
