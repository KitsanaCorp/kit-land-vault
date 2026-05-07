import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
  },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/dashboard/dashboard').then(m => m.Dashboard),
    canActivate: [authGuard]
  },
  {
    path: 'transactions/new',
    loadComponent: () => import('./features/transactions/transaction-form/transaction-form').then(m => m.TransactionForm),
    canActivate: [authGuard]
  },
  {
    path: 'transactions',
    loadComponent: () => import('./features/transactions/transaction-history/transaction-history').then(m => m.TransactionHistory),
    canActivate: [authGuard]
  },

  {
    path: '**',
    redirectTo: ''
  }
];
