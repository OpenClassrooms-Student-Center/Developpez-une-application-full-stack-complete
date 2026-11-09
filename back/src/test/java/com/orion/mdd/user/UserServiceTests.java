package com.orion.mdd.user;

import com.orion.mdd.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(userRepository, passwordEncoder);

    @Test
    void reportsAConflictWhenTheDatabaseRefusesTheChange() {
        // Arrange: another request took the username just after the checks
        User user = new User("arthur.dent", "arthur.dent@example.com", "hash");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("Duplicate entry"));
        ProfileRequest request = new ProfileRequest("ford.prefect", "ford.prefect@example.com", null);

        // Act and assert
        assertThatThrownBy(() -> userService.updateProfile(42L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Ce nom d’utilisateur ou cette adresse e-mail est déjà pris.");
    }

}
