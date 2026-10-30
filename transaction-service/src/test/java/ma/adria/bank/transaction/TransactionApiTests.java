package ma.adria.bank.transaction;

import ma.adria.bank.transaction.client.AccountDto;
import ma.adria.bank.transaction.service.AccountGateway;
import ma.adria.bank.transaction.service.AccountServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "bank.transaction.max-amount=1000"})
@AutoConfigureMockMvc
class TransactionApiTests {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AccountGateway accounts;

    private AccountDto account(UUID id, String status, String currency, double balance) {
        AccountDto dto = new AccountDto(id, "CUST", status, currency, balance);
        given(accounts.getAccount(id)).willReturn(dto);
        return dto;
    }

    private String transactionBody(UUID accountId, String amount, String currency) {
        return """
                {"accountId":"%s","amount":%s,"currency":"%s","merchantCategory":"GROCERY","country":"FR"}
                """.formatted(accountId, amount, currency);
    }

    private org.springframework.test.web.servlet.ResultActions postTransaction(String body) throws Exception {
        return mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void createTransactionDebitsAccount() throws Exception {
        UUID id = UUID.randomUUID();
        account(id, "ACTIVE", "EUR", 500);

        postTransaction(transactionBody(id, "250.00", "EUR"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(id.toString()))
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.amount").value(250.0));

        verify(accounts).debit(id, new BigDecimal("250.00"));
    }

    @Test
    void amountAboveCeilingIsRejected() throws Exception {
        UUID id = UUID.randomUUID();
        account(id, "ACTIVE", "EUR", 5000);

        postTransaction(transactionBody(id, "1000.01", "EUR")).andExpect(status().isUnprocessableEntity());
        verify(accounts, never()).debit(any(), any());
    }

    @Test
    void nonPositiveAmountIsRejected() throws Exception {
        UUID id = UUID.randomUUID();
        account(id, "ACTIVE", "EUR", 500);

        postTransaction(transactionBody(id, "0", "EUR")).andExpect(status().isUnprocessableEntity());
        postTransaction(transactionBody(id, "-5", "EUR")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void currencyMismatchIsRejected() throws Exception {
        UUID id = UUID.randomUUID();
        account(id, "ACTIVE", "EUR", 500);

        postTransaction(transactionBody(id, "10", "USD")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void blockedAccountIsRejected() throws Exception {
        UUID id = UUID.randomUUID();
        account(id, "BLOCKED", "EUR", 500);

        postTransaction(transactionBody(id, "10", "EUR")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void insufficientBalanceIsRejected() throws Exception {
        UUID id = UUID.randomUUID();
        account(id, "ACTIVE", "EUR", 5);

        postTransaction(transactionBody(id, "10", "EUR")).andExpect(status().isUnprocessableEntity());
        verify(accounts, never()).debit(any(), any());
    }

    @Test
    void accountServiceDownReturns503() throws Exception {
        UUID id = UUID.randomUUID();
        given(accounts.getAccount(id)).willThrow(new AccountServiceUnavailableException("down", null));

        postTransaction(transactionBody(id, "10", "EUR")).andExpect(status().isServiceUnavailable());
    }

    @Test
    void transferMovesFundsBetweenAccounts() throws Exception {
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        account(source, "ACTIVE", "EUR", 500);
        account(target, "ACTIVE", "EUR", 20);

        mvc.perform(post("/api/transactions/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceAccountId":"%s","targetAccountId":"%s","amount":100,"currency":"EUR","country":"FR"}
                                """.formatted(source, target)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].type").value("DEBIT"))
                .andExpect(jsonPath("$[1].type").value("CREDIT"));

        verify(accounts).debit(source, new BigDecimal("100"));
        verify(accounts).credit(target, new BigDecimal("100"));
    }

    @Test
    void transferToSameAccountIsRejected() throws Exception {
        UUID id = UUID.randomUUID();
        account(id, "ACTIVE", "EUR", 500);

        mvc.perform(post("/api/transactions/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceAccountId":"%s","targetAccountId":"%s","amount":10,"currency":"EUR","country":"FR"}
                                """.formatted(id, id)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void failedCreditRefundsSourceAccount() throws Exception {
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        account(source, "ACTIVE", "EUR", 500);
        account(target, "ACTIVE", "EUR", 20);
        given(accounts.credit(target, new BigDecimal("100")))
                .willThrow(new AccountServiceUnavailableException("down", null));

        mvc.perform(post("/api/transactions/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceAccountId":"%s","targetAccountId":"%s","amount":100,"currency":"EUR","country":"FR"}
                                """.formatted(source, target)))
                .andExpect(status().isServiceUnavailable());

        verify(accounts).credit(source, new BigDecimal("100"));
    }

    @Test
    void unknownTransactionReturnsNotFound() throws Exception {
        mvc.perform(get("/api/transactions/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }
}
