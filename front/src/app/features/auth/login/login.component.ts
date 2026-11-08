import { Component, inject, signal } from '@angular/core';
import { apply, form, FormField, FormRoot } from '@angular/forms/signals';
import { MatButton } from '@angular/material/button';
import { MatError, MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { Session } from '../../../core/session/session.service';
import { emailRules, requiredPasswordRules } from '../../../shared/forms/account-rules.schema';
import { failureMessage, fieldErrors } from '../../../shared/forms/server-errors.util';
import { AuthApi } from '../auth-api.service';
import { AuthLayout } from '../auth-layout/auth-layout.component';
import { LoginRequest } from '../login-request.model';

@Component({
  imports: [AuthLayout, FormField, FormRoot, MatButton, MatError, MatFormField, MatInput],
  selector: 'app-login',
  styleUrl: './login.component.scss',
  templateUrl: './login.component.html',
})
export class Login {
  private readonly authApi = inject(AuthApi);
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  private readonly credentials = signal<LoginRequest>({ email: '', password: '' });
  protected readonly failure = signal('');

  protected readonly loginForm = form(
    this.credentials,
    (path) => {
      apply(path.email, emailRules);
      apply(path.password, requiredPasswordRules);
    },
    {
      submission: {
        action: async (field) => {
          try {
            const user = await firstValueFrom(this.authApi.logIn(this.credentials()));
            this.session.logIn(user);
            await this.router.navigateByUrl('/feed');
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
