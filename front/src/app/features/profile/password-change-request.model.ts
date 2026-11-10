/**
 * The current password, which proves who is at the keyboard, and the new one.
 */
export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}
