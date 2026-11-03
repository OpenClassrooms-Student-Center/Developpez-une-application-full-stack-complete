import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { User } from '../../core/session/user';
import { RegisterRequest } from './register-request';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  register(request: RegisterRequest): Observable<User> {
    return this.http.post<User>('/api/auth/register', request);
  }
}
