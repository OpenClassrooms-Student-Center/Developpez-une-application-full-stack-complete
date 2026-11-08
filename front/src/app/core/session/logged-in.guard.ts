import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Session } from './session.service';

/**
 * Lets only a logged-in user in, and sends a visitor to the login page.
 */
export const loggedInGuard: CanActivateFn = () =>
  inject(Session).user() ? true : inject(Router).parseUrl('/login');
