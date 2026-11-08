package com.orion.mdd.auth;

import com.orion.mdd.common.validation.RequiredPassword;
import com.orion.mdd.common.validation.ValidEmail;

/**
 * The credentials sent to log in. The password is only required: the rules of the registration apply when a
 * password is chosen, so that an account still opens if they change.
 *
 * @param email
 *            the e-mail address of the account
 * @param password
 *            the password
 */
public record LoginRequest(@ValidEmail String email, @RequiredPassword String password) {

}
