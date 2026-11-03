import { Component, inject, signal } from '@angular/core';
import { apply, form, FormField, FormRoot } from '@angular/forms/signals';
import { MatButton } from '@angular/material/button';
import { MatError, MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { Session } from '../../../core/session/session';
import { emailRules, passwordRules, usernameRules } from '../../../shared/forms/account-rules';
import { AuthApi } from '../auth-api';
import { AuthLayout } from '../auth-layout/auth-layout';
import { RegisterRequest } from '../register-request';

@Component({
  imports: [AuthLayout, FormField, FormRoot, MatButton, MatError, MatFormField, MatInput],
  selector: 'app-register',
  styleUrl: './register.scss',
  templateUrl: './register.html',
})
export class Register {
  private readonly authApi = inject(AuthApi);
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  private readonly account = signal<RegisterRequest>({ username: '', email: '', password: '' });

  protected readonly registerForm = form(
    this.account,
    (path) => {
      apply(path.username, usernameRules);
      apply(path.email, emailRules);
      apply(path.password, passwordRules);
    },
    {
      submission: {
        action: async () => {
          const user = await firstValueFrom(this.authApi.register(this.account()));
          this.session.logIn(user);
          await this.router.navigateByUrl('/feed');
        },
      },
    },
  );
}
