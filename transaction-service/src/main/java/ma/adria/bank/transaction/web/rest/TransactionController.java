package ma.adria.bank.transaction.web.rest;

import jakarta.validation.Valid;
import ma.adria.bank.transaction.domain.Transaction;
import ma.adria.bank.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody CreateTransactionRequest request) {
        return service.create(request.accountId(), request.amount(), request.currency(),
                request.merchantCategory(), request.country());
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Transaction> transfer(@Valid @RequestBody TransferRequest request) {
        return service.transfer(request.sourceAccountId(), request.targetAccountId(), request.amount(),
                request.currency(), request.country());
    }

    @GetMapping
    public List<Transaction> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Transaction findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping("/account/{accountId}")
    public List<Transaction> findByAccount(@PathVariable UUID accountId) {
        return service.findByAccount(accountId);
    }
}
