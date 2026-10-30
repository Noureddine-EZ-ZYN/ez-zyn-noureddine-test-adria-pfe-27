package ma.adria.bank.account.web.graphql;

import ma.adria.bank.account.domain.Account;
import ma.adria.bank.account.domain.AccountStatus;
import ma.adria.bank.account.service.AccountNotFoundException;
import ma.adria.bank.account.service.AccountService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class AccountGraphQlController {

    private final AccountService service;

    public AccountGraphQlController(AccountService service) {
        this.service = service;
    }

    @QueryMapping
    public List<Account> accounts() {
        return service.findAll();
    }

    @QueryMapping
    public Account accountById(@Argument UUID id) {
        try {
            return service.findById(id);
        } catch (AccountNotFoundException ex) {
            return null;
        }
    }

    @MutationMapping
    public Account createAccount(@Argument CreateAccountInput input) {
        double initialBalance = input.initialBalance() == null ? 0 : input.initialBalance();
        return service.create(input.customerReference(), input.currency(), initialBalance);
    }

    @MutationMapping
    public Account updateAccountStatus(@Argument UUID id, @Argument AccountStatus status) {
        return service.updateStatus(id, status);
    }

    @MutationMapping
    public Account debitAccount(@Argument UUID id, @Argument double amount) {
        return service.debit(id, amount);
    }

    @MutationMapping
    public Account creditAccount(@Argument UUID id, @Argument double amount) {
        return service.credit(id, amount);
    }

    public record CreateAccountInput(String customerReference, String currency, Double initialBalance) {
    }
}
