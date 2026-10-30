package ma.adria.bank.account.service;

import ma.adria.bank.account.domain.Account;
import ma.adria.bank.account.domain.AccountStatus;
import ma.adria.bank.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account create(String customerReference, String currency, double initialBalance) {
        if (initialBalance < 0) {
            throw new InvalidOperationException("Le solde initial ne peut pas être négatif");
        }
        return repository.save(new Account(customerReference, currency.toUpperCase(), initialBalance));
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Account findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account updateStatus(UUID id, AccountStatus status) {
        Account account = findById(id);
        account.setStatus(status);
        return repository.save(account);
    }

    public Account debit(UUID id, double amount) {
        requirePositive(amount);
        Account account = findActive(id);
        if (account.getBalance() < amount) {
            throw new InvalidOperationException("Solde insuffisant sur le compte " + id);
        }
        account.setBalance(account.getBalance() - amount);
        return repository.save(account);
    }

    public Account credit(UUID id, double amount) {
        requirePositive(amount);
        Account account = findActive(id);
        account.setBalance(account.getBalance() + amount);
        return repository.save(account);
    }

    private Account findActive(UUID id) {
        Account account = findById(id);
        if (!account.isActive()) {
            throw new InvalidOperationException("Le compte " + id + " est bloqué");
        }
        return account;
    }

    private void requirePositive(double amount) {
        if (amount <= 0) {
            throw new InvalidOperationException("Le montant doit être strictement positif");
        }
    }
}
