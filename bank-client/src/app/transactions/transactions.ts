import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { BankApi, errorMessage } from '../api.service';
import { Account, Transaction } from '../models';

@Component({
  selector: 'app-transactions',
  imports: [FormsModule, DatePipe, DecimalPipe],
  templateUrl: './transactions.html',
})
export class Transactions {
  private readonly api = inject(BankApi);

  protected readonly accounts = signal<Account[]>([]);
  protected readonly transactions = signal<Transaction[]>([]);
  protected readonly filterAccountId = signal('');
  protected readonly error = signal('');
  protected readonly success = signal('');

  protected readonly accountId = signal('');
  protected readonly amount = signal(0);
  protected readonly currency = signal('EUR');
  protected readonly merchantCategory = signal('GROCERY');
  protected readonly country = signal('FR');

  protected readonly sourceId = signal('');
  protected readonly targetId = signal('');
  protected readonly transferAmount = signal(0);
  protected readonly transferCurrency = signal('EUR');
  protected readonly transferCountry = signal('FR');

  constructor() {
    this.api.listAccounts().subscribe({
      next: (accounts) => this.accounts.set(accounts),
      error: (err) => this.error.set(errorMessage(err)),
    });
    // Recharge la liste quand le filtre change (et au démarrage).
    effect(() => this.loadTransactions(this.filterAccountId()));
  }

  protected selectAccount(id: string) {
    this.accountId.set(id);
    const account = this.accounts().find((a) => a.id === id);
    if (account) {
      this.currency.set(account.currency);
    }
  }

  protected create() {
    this.run(
      this.api.createTransaction({
        accountId: this.accountId(),
        amount: this.amount(),
        currency: this.currency(),
        merchantCategory: this.merchantCategory(),
        country: this.country(),
      }),
      'Transaction enregistrée',
    );
  }

  protected transfer() {
    this.run(
      this.api.transfer({
        sourceAccountId: this.sourceId(),
        targetAccountId: this.targetId(),
        amount: this.transferAmount(),
        currency: this.transferCurrency(),
        country: this.transferCountry(),
      }),
      'Virement effectué',
    );
  }

  private loadTransactions(accountId: string) {
    this.api.listTransactions(accountId || undefined).subscribe({
      next: (transactions) => this.transactions.set(transactions),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  private run(request: Observable<unknown>, successMessage: string) {
    this.error.set('');
    this.success.set('');
    request.subscribe({
      next: () => {
        this.success.set(successMessage);
        this.loadTransactions(this.filterAccountId());
        this.api.listAccounts().subscribe((accounts) => this.accounts.set(accounts));
      },
      error: (err) => this.error.set(errorMessage(err)),
    });
  }
}
