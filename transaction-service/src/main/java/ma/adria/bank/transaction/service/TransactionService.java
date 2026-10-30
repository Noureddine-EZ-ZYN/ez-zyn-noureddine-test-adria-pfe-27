package ma.adria.bank.transaction.service;

import ma.adria.bank.transaction.client.AccountDto;
import ma.adria.bank.transaction.domain.Transaction;
import ma.adria.bank.transaction.domain.TransactionType;
import ma.adria.bank.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final String TRANSFER_CATEGORY = "TRANSFER";

    private final TransactionRepository repository;
    private final AccountGateway accounts;
    private final BigDecimal maxAmount;

    public TransactionService(TransactionRepository repository, AccountGateway accounts,
                              @Value("${bank.transaction.max-amount}") BigDecimal maxAmount) {
        this.repository = repository;
        this.accounts = accounts;
        this.maxAmount = maxAmount;
    }

    public Transaction create(UUID accountId, BigDecimal amount, String currency,
                              String merchantCategory, String country) {
        requireValidAmount(amount);
        AccountDto account = accounts.getAccount(accountId);
        requireUsable(account, currency);
        requireSufficientBalance(account, amount);

        Transaction transaction = repository.save(new Transaction(
                accountId, TransactionType.DEBIT, amount, currency.toUpperCase(), merchantCategory, country));
        accounts.debit(accountId, amount);
        return transaction;
    }

    public List<Transaction> transfer(UUID sourceId, UUID targetId, BigDecimal amount,
                                      String currency, String country) {
        if (sourceId.equals(targetId)) {
            throw new BusinessRuleException("Le compte source et le compte cible doivent être différents");
        }
        requireValidAmount(amount);
        AccountDto source = accounts.getAccount(sourceId);
        AccountDto target = accounts.getAccount(targetId);
        requireUsable(source, currency);
        requireUsable(target, currency);
        requireSufficientBalance(source, amount);

        String ccy = currency.toUpperCase();
        Transaction debit = repository.save(
                new Transaction(sourceId, TransactionType.DEBIT, amount, ccy, TRANSFER_CATEGORY, country));
        Transaction credit = repository.save(
                new Transaction(targetId, TransactionType.CREDIT, amount, ccy, TRANSFER_CATEGORY, country));

        accounts.debit(sourceId, amount);
        try {
            accounts.credit(targetId, amount);
        } catch (RuntimeException ex) {
            refund(sourceId, amount);
            throw ex;
        }
        return List.of(debit, credit);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByAccount(UUID accountId) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    private void refund(UUID sourceId, BigDecimal amount) {
        try {
            accounts.credit(sourceId, amount);
        } catch (RuntimeException refundFailure) {
            log.error("Échec du remboursement de {} sur le compte {} après un virement interrompu : "
                    + "intervention manuelle requise", amount, sourceId, refundFailure);
        }
    }

    private void requireValidAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("Le montant doit être strictement positif");
        }
        if (amount.compareTo(maxAmount) > 0) {
            throw new BusinessRuleException("Le montant dépasse le plafond autorisé de " + maxAmount);
        }
    }

    private void requireUsable(AccountDto account, String currency) {
        if (!account.isActive()) {
            throw new BusinessRuleException("Le compte " + account.id() + " n'est pas actif");
        }
        if (!account.currency().equalsIgnoreCase(currency)) {
            throw new BusinessRuleException("La devise " + currency + " ne correspond pas à celle du compte "
                    + account.id() + " (" + account.currency() + ")");
        }
    }

    private void requireSufficientBalance(AccountDto account, BigDecimal amount) {
        if (BigDecimal.valueOf(account.balance()).compareTo(amount) < 0) {
            throw new BusinessRuleException("Solde insuffisant sur le compte " + account.id());
        }
    }
}
