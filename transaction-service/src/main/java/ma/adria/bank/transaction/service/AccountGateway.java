package ma.adria.bank.transaction.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import ma.adria.bank.transaction.client.AccountClient;
import ma.adria.bank.transaction.client.AccountDto;
import ma.adria.bank.transaction.client.AmountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Point d'accès unique à account-service, protégé par Resilience4J.
 * Le retry ne porte que sur la lecture : un débit/crédit rejoué après un timeout
 * pourrait être appliqué deux fois.
 */
@Component
public class AccountGateway {

    private static final Logger log = LoggerFactory.getLogger(AccountGateway.class);

    private final AccountClient client;

    public AccountGateway(AccountClient client) {
        this.client = client;
    }

    @Retry(name = "accountService")
    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountFallback")
    public AccountDto getAccount(UUID accountId) {
        return client.getAccount(accountId);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "debitFallback")
    public AccountDto debit(UUID accountId, BigDecimal amount) {
        return client.debit(accountId, new AmountRequest(amount.doubleValue()));
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "creditFallback")
    public AccountDto credit(UUID accountId, BigDecimal amount) {
        return client.credit(accountId, new AmountRequest(amount.doubleValue()));
    }

    private AccountDto getAccountFallback(UUID accountId, Throwable cause) {
        throw unavailable("consultation du compte " + accountId, cause);
    }

    private AccountDto debitFallback(UUID accountId, BigDecimal amount, Throwable cause) {
        throw unavailable("débit du compte " + accountId, cause);
    }

    private AccountDto creditFallback(UUID accountId, BigDecimal amount, Throwable cause) {
        throw unavailable("crédit du compte " + accountId, cause);
    }

    private RuntimeException unavailable(String operation, Throwable cause) {
        if (cause instanceof BusinessRuleException || cause instanceof AccountNotFoundException) {
            return (RuntimeException) cause;
        }
        log.warn("account-service indisponible pour {} : {}", operation, cause.toString());
        return new AccountServiceUnavailableException(
                "account-service est indisponible (" + operation + "), réessayez plus tard", cause);
    }
}
