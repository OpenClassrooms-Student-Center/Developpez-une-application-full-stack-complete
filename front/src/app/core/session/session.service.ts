import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';

import { User } from './user.model';

@Injectable({ providedIn: 'root' })
export class Session {
  private readonly http = inject(HttpClient);

  private readonly currentUser = signal<User | null>(null);

  readonly user = this.currentUser.asReadonly();

  /**
   * Keeps the user the API has just logged in.
   */
  logIn(user: User): void {
    this.currentUser.set(user);
  }

  /**
   * Keeps the profile the API has just saved.
   */
  update(user: User): void {
    this.currentUser.set(user);
  }

  /**
   * Asks the API to delete the cookie, then forgets the user.
   */
  logOut(): Observable<void> {
    return this.http.post<void>('/api/auth/logout', null).pipe(map(() => this.forget()));
  }

  /**
   * Forgets the user once the API has deleted the cookie, after a logout or an account deletion.
   */
  forget(): void {
    this.currentUser.set(null);
  }

  /**
   * Asks the API who is logged in, since the HttpOnly cookie cannot be read here.
   * The response, even a 401, also brings the CSRF cookie that the first POST needs.
   */
  restore(): Observable<void> {
    return this.http.get<User>('/api/users/me').pipe(
      catchError(() => of(null)),
      map((user) => this.currentUser.set(user)),
    );
  }
}
