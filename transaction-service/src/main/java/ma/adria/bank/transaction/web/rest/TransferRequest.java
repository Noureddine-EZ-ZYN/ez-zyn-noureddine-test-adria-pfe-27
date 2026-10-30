package ma.adria.bank.transaction.web.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID sourceAccountId,
        @NotNull UUID targetAccountId,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String country) {
}
