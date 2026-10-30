package ma.adria.bank.account.web.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequest(
        @NotBlank String customerReference,
        @NotBlank String currency,
        @PositiveOrZero double initialBalance) {
}
