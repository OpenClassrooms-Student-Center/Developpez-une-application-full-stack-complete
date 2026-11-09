import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { User } from '../../core/session/user.model';
import { PasswordChangeRequest } from './password-change-request.model';
import { ProfileRequest } from './profile-request.model';

@Injectable({ providedIn: 'root' })
export class ProfileApi {
  private readonly http = inject(HttpClient);

  update(request: ProfileRequest): Observable<User> {
    return this.http.put<User>('/api/users/me', request);
  }

  changePassword(request: PasswordChangeRequest): Observable<void> {
    return this.http.put<void>('/api/users/me/password', request);
  }
}
