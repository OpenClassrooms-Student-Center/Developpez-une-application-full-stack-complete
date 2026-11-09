package com.orion.mdd.user;

import com.orion.mdd.common.exception.ConflictException;
import com.orion.mdd.common.exception.InvalidFieldException;
import com.orion.mdd.common.exception.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Gives access to the logged-in user and lets them change their profile and their password, or delete their account.
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
     * Changes the username and the e-mail address. The password has its own method, which asks for the current one.
     *
     * @param id
     *            the identifier carried by the token
     * @param request
     *            the new values
     * @return the saved user
     * @throws UserNotFoundException
     *             if the account no longer exists
     * @throws ConflictException
     *             if another user already has the username or the e-mail, even through a request running at the
     *             same moment
     */
    public User updateProfile(Long id, ProfileRequest request) {
        User user = getCurrentUser(id);

        if (userRepository.existsByUsernameAndIdNot(request.username(), id)) {
            throw new ConflictException("username", "Ce nom d’utilisateur est déjà pris.");
        }
        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("email", "Cette adresse e-mail est déjà utilisée.");
        }

        user.changeProfile(request.username(), request.email());
        try {
            // Flushing here makes the unique constraints answer now, inside this try, not at the commit
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Ce nom d’utilisateur ou cette adresse e-mail est déjà pris.");
        }
    }

    /**
     * Replaces the password once the current one has been confirmed.
     *
     * @param id
     *            the identifier carried by the token
     * @param request
     *            the current password and the new one
     * @throws UserNotFoundException
     *             if the account no longer exists
     * @throws InvalidFieldException
     *             if the current password is wrong
     */
    public void changePassword(Long id, PasswordChangeRequest request) {
        User user = getCurrentUser(id);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidFieldException("currentPassword", "Le mot de passe actuel est incorrect.");
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    /**
     * Deletes the account. The database deletes its subscriptions, comments and posts in cascade.
     *
     * @param id
     *            the identifier carried by the token
     */
    public void deleteAccount(Long id) {
        userRepository.deleteById(id);
    }

}
