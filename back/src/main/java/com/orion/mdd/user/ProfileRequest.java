package com.orion.mdd.user;

/**
 * The data sent to change the profile. The password has its own request, which asks for the current one.
 *
 * @param username
 *            the new public name
 * @param email
 *            the new e-mail address
 */
public record ProfileRequest(String username, String email) {

}
