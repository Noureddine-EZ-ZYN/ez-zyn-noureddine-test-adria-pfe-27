package ma.adria.bank.transaction.mcp;

import ma.adria.bank.transaction.domain.Transaction;
import ma.adria.bank.transaction.service.TransactionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionMcpTools {

    private final TransactionService service;

    public TransactionMcpTools(TransactionService service) {
        this.service = service;
    }

    @Tool(name = "list_transactions", description = "Liste toutes les transactions enregistrées")
    public List<Transaction> listTransactions() {
        return service.findAll();
    }

    @Tool(name = "get_transaction", description = "Consulte une transaction à partir de son identifiant")
    public Transaction getTransaction(@ToolParam(description = "Identifiant UUID de la transaction") String transactionId) {
        return service.findById(UUID.fromString(transactionId));
    }

    @Tool(name = "list_account_transactions", description = "Liste les transactions d'un compte, les plus récentes d'abord")
    public List<Transaction> listAccountTransactions(@ToolParam(description = "Identifiant UUID du compte") String accountId) {
        return service.findByAccount(UUID.fromString(accountId));
    }

    @Tool(name = "create_transaction", description = "Enregistre une transaction (paiement) qui débite un compte actif. "
            + "Le montant doit être positif et inférieur au plafond, la devise identique à celle du compte")
    public Transaction createTransaction(
            @ToolParam(description = "Identifiant UUID du compte") String accountId,
            @ToolParam(description = "Montant de la transaction") double amount,
            @ToolParam(description = "Code devise, par exemple EUR") String currency,
            @ToolParam(description = "Catégorie du commerçant, par exemple GROCERY") String merchantCategory,
            @ToolParam(description = "Code pays, par exemple FR ou MA") String country) {
        return service.create(UUID.fromString(accountId), BigDecimal.valueOf(amount), currency,
                merchantCategory, country);
    }

    @Tool(name = "transfer_funds", description = "Effectue un virement d'un compte vers un autre compte de même devise")
    public List<Transaction> transferFunds(
            @ToolParam(description = "Identifiant UUID du compte source") String sourceAccountId,
            @ToolParam(description = "Identifiant UUID du compte cible") String targetAccountId,
            @ToolParam(description = "Montant du virement") double amount,
            @ToolParam(description = "Code devise, par exemple EUR") String currency,
            @ToolParam(description = "Code pays, par exemple FR ou MA") String country) {
        return service.transfer(UUID.fromString(sourceAccountId), UUID.fromString(targetAccountId),
                BigDecimal.valueOf(amount), currency, country);
    }
}
