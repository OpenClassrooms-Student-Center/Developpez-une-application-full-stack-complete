package com.orion.mdd.auth;

import com.orion.mdd.common.exception.ConflictException;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Creates the accounts.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates an account whose password is stored hashed.
     *
     * @param request
     *            the data of the new user
     * @return the saved user
     * @throws ConflictException
     *             if the username or the e-mail is already taken
     */
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("username", "Ce nom d’utilisateur est déjà pris.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("email", "Cette adresse e-mail est déjà utilisée.");
        }

        User user = new User(request.username(), request.email(), passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }

}
