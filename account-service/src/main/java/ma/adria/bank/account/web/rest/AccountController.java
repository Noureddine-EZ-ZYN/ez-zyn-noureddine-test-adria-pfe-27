package ma.adria.bank.account.web.rest;

import jakarta.validation.Valid;
import ma.adria.bank.account.domain.Account;
import ma.adria.bank.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@Valid @RequestBody CreateAccountRequest request) {
        return service.create(request.customerReference(), request.currency(), request.initialBalance());
    }

    @GetMapping
    public List<Account> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Account findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/status")
    public Account updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        return service.updateStatus(id, request.status());
    }

    @PostMapping("/{id}/debit")
    public Account debit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return service.debit(id, request.amount());
    }

    @PostMapping("/{id}/credit")
    public Account credit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return service.credit(id, request.amount());
    }
}
