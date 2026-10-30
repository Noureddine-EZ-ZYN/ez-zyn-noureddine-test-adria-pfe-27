package ma.adria.bank.account.web.rest;

import jakarta.validation.constraints.NotNull;
import ma.adria.bank.account.domain.AccountStatus;

public record UpdateStatusRequest(@NotNull AccountStatus status) {
}
