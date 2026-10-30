export type AccountStatus = 'ACTIVE' | 'BLOCKED';

export interface Account {
  id: string;
  customerReference: string;
  status: AccountStatus;
  currency: string;
  balance: number;
}

export interface Transaction {
  id: string;
  accountId: string;
  type: 'DEBIT' | 'CREDIT';
  amount: number;
  currency: string;
  merchantCategory: string;
  country: string;
  createdAt: string;
}

export interface CreateAccountRequest {
  customerReference: string;
  currency: string;
  initialBalance: number;
}

export interface CreateTransactionRequest {
  accountId: string;
  amount: number;
  currency: string;
  merchantCategory: string;
  country: string;
}

export interface TransferRequest {
  sourceAccountId: string;
  targetAccountId: string;
  amount: number;
  currency: string;
  country: string;
}

export interface ChatResponse {
  conversationId: string;
  reply: string;
}
