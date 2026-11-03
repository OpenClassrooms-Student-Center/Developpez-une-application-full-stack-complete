import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';

import { User } from './user';

@Injectable({ providedIn: 'root' })
export class Session {
  private readonly http = inject(HttpClient);

  readonly user = signal<User | null>(null);

  /**
   * Asks the API who is logged in, since the HttpOnly cookie cannot be read here.
   * The response, even a 401, also brings the CSRF cookie that the first POST needs.
   */
  restore(): Observable<void> {
    return this.http.get<User>('/api/users/me').pipe(
      catchError(() => of(null)),
      map((user) => this.user.set(user)),
    );
  }
}
