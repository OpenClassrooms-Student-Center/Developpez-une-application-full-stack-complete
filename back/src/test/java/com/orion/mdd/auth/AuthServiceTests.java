package com.orion.mdd.auth;

import com.orion.mdd.common.exception.ConflictException;
import com.orion.mdd.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthService authService = new AuthService(userRepository, passwordEncoder);

    @Test
    void reportsAConflictWhenTheDatabaseRefusesTheUser() {
        // Arrange: another request took the username just after the checks
        RegisterRequest request = new RegisterRequest("arthur.dent", "arthur.dent@example.com", "Dont-Panic-42");
        when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act and assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Ce nom d’utilisateur ou cette adresse e-mail est déjà pris.");
    }

}
