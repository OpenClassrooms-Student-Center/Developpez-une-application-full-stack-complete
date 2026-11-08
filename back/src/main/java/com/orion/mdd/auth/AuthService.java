package com.orion.mdd.auth;

import com.orion.mdd.common.exception.ConflictException;
import com.orion.mdd.common.exception.InvalidCredentialsException;
import com.orion.mdd.user.User;
import com.orion.mdd.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Creates the accounts and checks the credentials.
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
     * <p>
     * Two people can pick the same username at the same moment: both pass the checks below, and the unique
     * constraints of the database then refuse the second one.
     *
     * @param request
     *            the data of the new user
     * @return the saved user
     * @throws ConflictException
     *             if the username or the e-mail is already taken, even by a request running at the same moment
     */
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("username", "Ce nom d’utilisateur est déjà pris.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("email", "Cette adresse e-mail est déjà utilisée.");
        }

        User user = new User(request.username(), request.email(), passwordEncoder.encode(request.password()));
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Ce nom d’utilisateur ou cette adresse e-mail est déjà pris.");
        }
    }

    /**
     * Finds the user whose e-mail address and password match.
     *
     * @param request
     *            the credentials
     * @return the user
     * @throws InvalidCredentialsException
     *             if no account has this e-mail address and password
     */
    public User login(LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
    }

}
