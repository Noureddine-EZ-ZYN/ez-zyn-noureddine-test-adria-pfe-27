import { Routes } from '@angular/router';
import { Accounts } from './accounts/accounts';
import { Assistant } from './assistant/assistant';
import { Transactions } from './transactions/transactions';

export const routes: Routes = [
  { path: '', redirectTo: 'accounts', pathMatch: 'full' },
  { path: 'accounts', component: Accounts },
  { path: 'transactions', component: Transactions },
  { path: 'assistant', component: Assistant },
];
