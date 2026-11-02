package com.orion.mdd.auth;

import com.orion.mdd.common.validation.ValidEmail;
import com.orion.mdd.common.validation.ValidPassword;
import com.orion.mdd.common.validation.ValidUsername;

/**
 * The data sent to create an account, checked against the rules of the data definition annex.
 *
 * @param username
 *            the public name of the user
 * @param email
 *            the e-mail address used to log in
 * @param password
 *            the password
 */
public record RegisterRequest(@ValidUsername String username, @ValidEmail String email,
        @ValidPassword String password) {

}
