import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Session } from './session.service';

/**
 * Keeps the home, login and registration pages for visitors, and sends a logged-in user to the feed.
 */
export const loggedOutGuard: CanActivateFn = () =>
  inject(Session).user() ? inject(Router).parseUrl('/feed') : true;
