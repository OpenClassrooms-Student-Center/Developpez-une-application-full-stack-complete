import { Component, inject, signal } from '@angular/core';
import { apply, form, FormField, FormRoot } from '@angular/forms/signals';
import { MatButton } from '@angular/material/button';
import { MatError, MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';

import { Session } from '../../../core/session/session.service';
import { emailRules, usernameRules } from '../../../shared/forms/account-rules.schema';
import { submissionErrors } from '../../../shared/forms/server-errors.util';
import { ProfileApi } from '../profile-api.service';
import { ProfileRequest } from '../profile-request.model';

/**
 * Changes the username and the e-mail address, under the same rules as the registration.
 */
@Component({
  imports: [FormField, FormRoot, MatButton, MatError, MatFormField, MatInput],
  selector: 'app-profile-form',
  styleUrl: './profile-form.component.scss',
  templateUrl: './profile-form.component.html',
})
export class ProfileForm {
  private readonly profileApi = inject(ProfileApi);
  private readonly session = inject(Session);
  private readonly snackBar = inject(MatSnackBar);

  private readonly profile = signal<ProfileRequest>({
    username: this.session.user()?.username ?? '',
    email: this.session.user()?.email ?? '',
  });
  protected readonly failure = signal('');

  protected readonly profileForm = form(
    this.profile,
    (path) => {
      apply(path.username, usernameRules);
      apply(path.email, emailRules);
    },
    {
      submission: {
        action: async (field) => {
          try {
            const user = await firstValueFrom(this.profileApi.update(this.profile()));
            this.session.update(user);
            this.failure.set('');
            this.snackBar.open('Profil mis à jour.', undefined, { duration: 3000 });
            return undefined;
          } catch (error) {
            return submissionErrors(error, field, this.failure);
          }
        },
      },
    },
  );
}
