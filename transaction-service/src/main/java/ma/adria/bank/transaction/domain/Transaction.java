package ma.adria.bank.transaction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bank_transaction")
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID accountId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    private String currency;

    private String merchantCategory;

    private String country;

    private Instant createdAt;

    protected Transaction() {
    }

    public Transaction(UUID accountId, TransactionType type, BigDecimal amount, String currency,
                       String merchantCategory, String country) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.merchantCategory = merchantCategory;
        this.country = country;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public String getCountry() {
        return country;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
