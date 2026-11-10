package com.orion.mdd.user;

import com.orion.mdd.common.validation.ValidEmail;
import com.orion.mdd.common.validation.ValidUsername;

/**
 * The data sent to change the profile, checked with the rules of the registration. The password has its own request,
 * which asks for the current one.
 *
 * @param username
 *            the new public name
 * @param email
 *            the new e-mail address
 */
public record ProfileRequest(@ValidUsername String username, @ValidEmail String email) {

}
