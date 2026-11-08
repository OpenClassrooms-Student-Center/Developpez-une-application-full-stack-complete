package com.orion.mdd.user;

import com.orion.mdd.common.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Gives access to the logged-in user.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

}
