import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { BankApi, errorMessage } from '../api.service';
import { Account } from '../models';

@Component({
  selector: 'app-accounts',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './accounts.html',
})
export class Accounts {
  private readonly api = inject(BankApi);

  protected readonly accounts = signal<Account[]>([]);
  protected readonly selectedId = signal<string | null>(null);
  protected readonly selected = computed(
    () => this.accounts().find((a) => a.id === this.selectedId()) ?? null,
  );
  protected readonly error = signal('');
  protected readonly success = signal('');

  protected readonly customerReference = signal('');
  protected readonly currency = signal('EUR');
  protected readonly initialBalance = signal(0);
  protected readonly amount = signal(0);

  constructor() {
    this.load();
  }

  protected load() {
    this.api.listAccounts().subscribe({
      next: (accounts) => this.accounts.set(accounts),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  protected create() {
    this.run(
      this.api.createAccount({
        customerReference: this.customerReference(),
        currency: this.currency(),
        initialBalance: this.initialBalance(),
      }),
      'Compte créé',
      (account) => {
        this.selectedId.set(account.id);
        this.customerReference.set('');
        this.initialBalance.set(0);
      },
    );
  }

  protected credit(account: Account) {
    this.run(this.api.credit(account.id, this.amount()), 'Compte crédité');
  }

  protected debit(account: Account) {
    this.run(this.api.debit(account.id, this.amount()), 'Compte débité');
  }

  protected toggleStatus(account: Account) {
    const status = account.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE';
    this.run(this.api.updateStatus(account.id, status), status === 'BLOCKED' ? 'Compte bloqué' : 'Compte activé');
  }

  private run(request: Observable<Account>, successMessage: string, after?: (account: Account) => void) {
    this.error.set('');
    this.success.set('');
    request.subscribe({
      next: (account) => {
        this.success.set(successMessage);
        after?.(account);
        this.load();
      },
      error: (err) => this.error.set(errorMessage(err)),
    });
  }
}
