package com.orion.mdd.user;

import org.jspecify.annotations.Nullable;

/**
 * The data sent to change the profile. The form always sends the username and the e-mail address.
 *
 * @param username
 *            the new public name
 * @param email
 *            the new e-mail address
 * @param password
 *            the new password, or {@code null} to keep the current one
 */
public record ProfileRequest(String username, String email, @Nullable String password) {

}
