package ma.adria.bank.account.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.util.UUID;

@Entity
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    private String customerReference;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private String currency;

    private double balance;

    @Version
    private Long version;

    protected Account() {
    }

    public Account(String customerReference, String currency, double balance) {
        this.customerReference = customerReference;
        this.currency = currency;
        this.balance = balance;
        this.status = AccountStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
