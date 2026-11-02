package com.orion.mdd.user;

/**
 * The profile of a user, as the API returns it. It never contains the password.
 *
 * @param id
 *            the user's identifier
 * @param username
 *            the user's public name
 * @param email
 *            the e-mail address used to log in
 */
public record UserResponse(Long id, String username, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

}
