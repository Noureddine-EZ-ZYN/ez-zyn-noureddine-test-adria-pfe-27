package ma.adria.bank.account.mcp;

import ma.adria.bank.account.domain.Account;
import ma.adria.bank.account.domain.AccountStatus;
import ma.adria.bank.account.service.AccountService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AccountMcpTools {

    private final AccountService service;

    public AccountMcpTools(AccountService service) {
        this.service = service;
    }

    @Tool(name = "list_accounts", description = "Liste tous les comptes bancaires")
    public List<Account> listAccounts() {
        return service.findAll();
    }

    @Tool(name = "get_account", description = "Consulte un compte bancaire (statut, devise, solde) à partir de son identifiant")
    public Account getAccount(@ToolParam(description = "Identifiant UUID du compte") String accountId) {
        return service.findById(UUID.fromString(accountId));
    }

    @Tool(name = "create_account", description = "Crée un nouveau compte bancaire actif")
    public Account createAccount(
            @ToolParam(description = "Référence du client") String customerReference,
            @ToolParam(description = "Code devise, par exemple EUR ou MAD") String currency,
            @ToolParam(description = "Solde initial", required = false) Double initialBalance) {
        return service.create(customerReference, currency, initialBalance == null ? 0 : initialBalance);
    }

    @Tool(name = "update_account_status", description = "Active ou bloque un compte (ACTIVE ou BLOCKED)")
    public Account updateAccountStatus(
            @ToolParam(description = "Identifiant UUID du compte") String accountId,
            @ToolParam(description = "ACTIVE ou BLOCKED") String status) {
        return service.updateStatus(UUID.fromString(accountId), AccountStatus.valueOf(status.toUpperCase()));
    }

    @Tool(name = "debit_account", description = "Débite un compte actif d'un montant strictement positif")
    public Account debitAccount(
            @ToolParam(description = "Identifiant UUID du compte") String accountId,
            @ToolParam(description = "Montant à débiter") double amount) {
        return service.debit(UUID.fromString(accountId), amount);
    }

    @Tool(name = "credit_account", description = "Crédite un compte actif d'un montant strictement positif")
    public Account creditAccount(
            @ToolParam(description = "Identifiant UUID du compte") String accountId,
            @ToolParam(description = "Montant à créditer") double amount) {
        return service.credit(UUID.fromString(accountId), amount);
    }
}
