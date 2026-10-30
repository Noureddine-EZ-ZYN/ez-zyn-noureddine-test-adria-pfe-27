package ma.adria.bank.transaction.service;

public class AccountServiceUnavailableException extends RuntimeException {

    public AccountServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
