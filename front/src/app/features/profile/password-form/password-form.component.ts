import { Component, inject, signal } from '@angular/core';
import { apply, form, FormField, FormRoot } from '@angular/forms/signals';
import { MatButton } from '@angular/material/button';
import { MatError, MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';

import { passwordRules, requiredPasswordRules } from '../../../shared/forms/account-rules.schema';
import { failureMessage, fieldErrors } from '../../../shared/forms/server-errors.util';
import { PasswordChangeRequest } from '../password-change-request.model';
import { ProfileApi } from '../profile-api.service';

const EMPTY: PasswordChangeRequest = { currentPassword: '', newPassword: '' };

/**
 * Changes the password, apart from the profile: the current one proves who is at the keyboard.
 */
@Component({
  imports: [FormField, FormRoot, MatButton, MatError, MatFormField, MatInput],
  selector: 'app-password-form',
  styleUrl: './password-form.component.scss',
  templateUrl: './password-form.component.html',
})
export class PasswordForm {
  private readonly profileApi = inject(ProfileApi);
  private readonly snackBar = inject(MatSnackBar);

  private readonly passwords = signal<PasswordChangeRequest>(EMPTY);
  protected readonly failure = signal('');

  protected readonly passwordForm = form(
    this.passwords,
    (path) => {
      apply(path.currentPassword, requiredPasswordRules);
      apply(path.newPassword, passwordRules);
    },
    {
      submission: {
        action: async (field) => {
          try {
            await firstValueFrom(this.profileApi.changePassword(this.passwords()));
            this.failure.set('');
            this.passwordForm().reset(EMPTY);
            this.snackBar.open('Mot de passe modifié.', undefined, { duration: 3000 });

            return undefined;
          } catch (error) {
            const errors = fieldErrors(error, field);
            this.failure.set(errors.length > 0 ? '' : failureMessage(error));
            return errors;
          }
        },
      },
    },
  );
}
