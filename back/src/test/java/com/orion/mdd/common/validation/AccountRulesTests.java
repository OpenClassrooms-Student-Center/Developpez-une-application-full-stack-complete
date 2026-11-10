package com.orion.mdd.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the rules of the account fields once, without Spring or a database. The registration and the profile only
 * check that they apply them.
 */
class AccountRulesTests {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /** Carries the rules as the request records do. */
    private record Account(@ValidUsername String username, @ValidEmail String email, @ValidPassword String password) {
    }

    @AfterAll
    static void closeTheFactory() {
        FACTORY.close();
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void refusesAnInvalidValueWithTheRuleItBreaks(String field, String value, Set<String> brokenRules) {
        // Act
        Set<ConstraintViolation<Account>> violations = VALIDATOR.validateValue(Account.class, field, value);

        // Assert: the name of each broken rule, such as Pattern or Size
        assertThat(violations.stream()
                .map(violation -> violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                .collect(Collectors.toSet())).isEqualTo(brokenRules);
    }

    private static Stream<Arguments> invalidValues() {
        return Stream.of(
                Arguments.of("username", "   ", Set.of("NotBlank", "Pattern")),
                Arguments.of("username", "42", Set.of("Pattern")),
                Arguments.of("username", "zaphod.beeblebrox.the.president", Set.of("Pattern")),
                Arguments.of("username", "arthur.dent@home", Set.of("Pattern")),
                Arguments.of("username", "marvin.déprimé", Set.of("Pattern")),
                Arguments.of("email", "   ", Set.of("NotBlank", "Email")),
                Arguments.of("email", "arthur.dent", Set.of("Email")),
                Arguments.of("email", "dent@" + "vogon.".repeat(39) + "example.co.uk.fr", Set.of("Size")),
                Arguments.of("password", "   ", Set.of("NotBlank", "Pattern")),
                Arguments.of("password", "Deep-42", Set.of("Pattern")),
                Arguments.of("password", "Dont-Panic-42".repeat(5), Set.of("Pattern")),
                Arguments.of("password", "dont-panic-42", Set.of("Pattern")),
                Arguments.of("password", "DONT-PANIC-42", Set.of("Pattern")),
                Arguments.of("password", "Dont-Panic", Set.of("Pattern")),
                Arguments.of("password", "DontPanic42", Set.of("Pattern")));
    }

    @ParameterizedTest
    @MethodSource("validValues")
    void acceptsAValidValueEvenAtTheLimits(String field, String value) {
        // Act
        Set<ConstraintViolation<Account>> violations = VALIDATOR.validateValue(Account.class, field, value);

        // Assert
        assertThat(violations).isEmpty();
    }

    private static Stream<Arguments> validValues() {
        return Stream.of(
                Arguments.of("username", "ade"),
                Arguments.of("username", "zaphod.beeblebrox_president-42"),
                Arguments.of("email", "arthur.dent@example.com"),
                Arguments.of("email", "dent@" + "vogon.".repeat(39) + "examples.com.fr"),
                Arguments.of("password", "Deep-42!"),
                Arguments.of("password", "Aa1-" + "x".repeat(60)));
    }

    @Test
    void refusesAMissingValue() {
        // Act
        Set<ConstraintViolation<Account>> violations = VALIDATOR.validate(new Account(null, null, null));

        // Assert: one message per field, so that each one shows under its own field
        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("username", "email", "password");
    }

}
