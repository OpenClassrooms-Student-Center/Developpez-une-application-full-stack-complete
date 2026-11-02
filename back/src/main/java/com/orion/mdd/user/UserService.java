package com.orion.mdd.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
     * @throws ResponseStatusException
     *             with a 401 status if the account no longer exists
     */
    public User getCurrentUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté."));
    }

}
