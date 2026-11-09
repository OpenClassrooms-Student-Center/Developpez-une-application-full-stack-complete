package com.orion.mdd.user;

import com.orion.mdd.common.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Gives access to the logged-in user and lets them change their profile.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds the user a token was issued to.
     *
     * @param id
     *            the identifier carried by the token
     * @return the user
     * @throws UserNotFoundException
     *             if the account no longer exists
     */
    public User getCurrentUser(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    /**
     * Changes the username and the e-mail address, and the password only when a new one is given.
     *
     * @param id
     *            the identifier carried by the token
     * @param request
     *            the new values
     * @return the saved user
     * @throws UserNotFoundException
     *             if the account no longer exists
     */
    public User updateProfile(Long id, ProfileRequest request) {
        User user = getCurrentUser(id);
        user.changeProfile(request.username(), request.email());
        String password = request.password();
        if (password != null) {
            user.changePassword(passwordEncoder.encode(password));
        }
        return userRepository.save(user);
    }

}
