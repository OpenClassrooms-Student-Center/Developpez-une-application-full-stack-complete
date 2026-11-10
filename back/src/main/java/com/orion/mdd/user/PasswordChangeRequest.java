package com.orion.mdd.user;

import com.orion.mdd.common.validation.RequiredPassword;
import com.orion.mdd.common.validation.ValidPassword;

/**
 * The data sent to change the password. The current password proves that the person at the keyboard owns the
 * account, and not someone who found the session open.
 *
 * @param currentPassword
 *            the password used until now
 * @param newPassword
 *            the new password, checked with the rules of the registration
 */
public record PasswordChangeRequest(@RequiredPassword String currentPassword, @ValidPassword String newPassword) {

}
