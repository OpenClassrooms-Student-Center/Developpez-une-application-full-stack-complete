import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/home/home').then((m) => m.Home),
  },
  {
    path: 'register',
    title: 'Inscription – MDD',
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
  },
  {
    path: 'feed',
    title: 'Fil d’actualité – MDD',
    loadComponent: () => import('./features/feed/feed').then((m) => m.Feed),
  },
];
