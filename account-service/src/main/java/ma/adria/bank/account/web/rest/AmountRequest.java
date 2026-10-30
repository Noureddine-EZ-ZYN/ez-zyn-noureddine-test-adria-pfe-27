package ma.adria.bank.account.web.rest;

import jakarta.validation.constraints.Positive;

public record AmountRequest(@Positive double amount) {
}
