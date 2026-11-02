package com.orion.mdd.auth;

/**
 * The data sent to create an account.
 *
 * @param username
 *            the public name of the user
 * @param email
 *            the e-mail address used to log in
 * @param password
 *            the password
 */
public record RegisterRequest(String username, String email, String password) {

}
