import { Component } from '@angular/core';

import { PasswordForm } from './password-form/password-form.component';
import { ProfileForm } from './profile-form/profile-form.component';

/**
 * The profile page: its title, then one section per change the user can make to their account.
 */
@Component({
  imports: [PasswordForm, ProfileForm],
  selector: 'app-profile',
  styleUrl: './profile.component.scss',
  templateUrl: './profile.component.html',
})
export class Profile {}
