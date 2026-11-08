import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { User } from '../../core/session/user.model';
import { LoginRequest } from './login-request.model';
import { RegisterRequest } from './register-request.model';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  register(request: RegisterRequest): Observable<User> {
    return this.http.post<User>('/api/auth/register', request);
  }

  logIn(request: LoginRequest): Observable<User> {
    return this.http.post<User>('/api/auth/login', request);
  }
}
