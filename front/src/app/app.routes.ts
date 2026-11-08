import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/home/home.component').then((m) => m.Home),
  },
  {
    path: 'login',
    title: 'Connexion – MDD',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.Login),
  },
  {
    path: 'register',
    title: 'Inscription – MDD',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.Register),
  },
  {
    path: 'feed',
    title: 'Fil d’actualité – MDD',
    loadComponent: () => import('./features/feed/feed.component').then((m) => m.Feed),
  },
];
