package ma.adria.bank.transaction.web.graphql;

import ma.adria.bank.transaction.domain.Transaction;
import ma.adria.bank.transaction.service.TransactionNotFoundException;
import ma.adria.bank.transaction.service.TransactionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
public class TransactionGraphQlController {

    private final TransactionService service;

    public TransactionGraphQlController(TransactionService service) {
        this.service = service;
    }

    @QueryMapping
    public List<Transaction> transactions() {
        return service.findAll();
    }

    @QueryMapping
    public Transaction transactionById(@Argument UUID id) {
        try {
            return service.findById(id);
        } catch (TransactionNotFoundException ex) {
            return null;
        }
    }

    @QueryMapping
    public List<Transaction> transactionsByAccount(@Argument UUID accountId) {
        return service.findByAccount(accountId);
    }

    @MutationMapping
    public Transaction createTransaction(@Argument CreateTransactionInput input) {
        return service.create(input.accountId(), input.amount(), input.currency(),
                input.merchantCategory(), input.country());
    }

    @MutationMapping
    public List<Transaction> transfer(@Argument TransferInput input) {
        return service.transfer(input.sourceAccountId(), input.targetAccountId(), input.amount(),
                input.currency(), input.country());
    }

    public record CreateTransactionInput(UUID accountId, BigDecimal amount, String currency,
                                         String merchantCategory, String country) {
    }

    public record TransferInput(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount,
                                String currency, String country) {
    }
}
