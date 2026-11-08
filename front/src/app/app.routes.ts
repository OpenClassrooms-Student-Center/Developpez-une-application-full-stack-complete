import { Routes } from '@angular/router';

import { loggedInGuard } from './core/session/logged-in.guard';
import { loggedOutGuard } from './core/session/logged-out.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [loggedOutGuard],
    loadComponent: () => import('./features/home/home.component').then((m) => m.Home),
  },
  {
    path: 'login',
    title: 'Connexion – MDD',
    canActivate: [loggedOutGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.Login),
  },
  {
    path: 'register',
    title: 'Inscription – MDD',
    canActivate: [loggedOutGuard],
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.Register),
  },
  {
    path: 'feed',
    title: 'Fil d’actualité – MDD',
    canActivate: [loggedInGuard],
    loadComponent: () => import('./features/feed/feed.component').then((m) => m.Feed),
  },
];
