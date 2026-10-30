import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {
  Account,
  AccountStatus,
  ChatResponse,
  CreateAccountRequest,
  CreateTransactionRequest,
  Transaction,
  TransferRequest,
} from './models';

// Toutes les requêtes passent par le Spring Cloud Gateway.
const GATEWAY = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class BankApi {
  private readonly http = inject(HttpClient);

  listAccounts() {
    return this.http.get<Account[]>(`${GATEWAY}/api/accounts`);
  }

  createAccount(request: CreateAccountRequest) {
    return this.http.post<Account>(`${GATEWAY}/api/accounts`, request);
  }

  updateStatus(id: string, status: AccountStatus) {
    return this.http.patch<Account>(`${GATEWAY}/api/accounts/${id}/status`, { status });
  }

  debit(id: string, amount: number) {
    return this.http.post<Account>(`${GATEWAY}/api/accounts/${id}/debit`, { amount });
  }

  credit(id: string, amount: number) {
    return this.http.post<Account>(`${GATEWAY}/api/accounts/${id}/credit`, { amount });
  }

  listTransactions(accountId?: string) {
    const url = accountId
      ? `${GATEWAY}/api/transactions/account/${accountId}`
      : `${GATEWAY}/api/transactions`;
    return this.http.get<Transaction[]>(url);
  }

  createTransaction(request: CreateTransactionRequest) {
    return this.http.post<Transaction>(`${GATEWAY}/api/transactions`, request);
  }

  transfer(request: TransferRequest) {
    return this.http.post<Transaction[]>(`${GATEWAY}/api/transactions/transfers`, request);
  }

  chat(message: string, conversationId: string | null) {
    return this.http.post<ChatResponse>(`${GATEWAY}/api/chat`, {
      message,
      conversationId,
    });
  }
}

/** Extrait un message lisible d'une erreur HTTP (ProblemDetail du back-end ou erreur du gateway). */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Serveur injoignable : le gateway est-il démarré ?';
    }
    return err.error?.detail ?? err.error?.error ?? `Erreur ${err.status}`;
  }
  return 'Erreur inattendue';
}
