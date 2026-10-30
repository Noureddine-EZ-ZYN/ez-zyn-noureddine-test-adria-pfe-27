package ma.adria.bank.transaction.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import ma.adria.bank.transaction.service.AccountNotFoundException;
import ma.adria.bank.transaction.service.BusinessRuleException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Traduit les réponses 4xx d'account-service en exceptions métier (qui ne comptent pas
 * comme des pannes pour le disjoncteur). Les 5xx restent des FeignException.
 */
@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder accountErrorDecoder(ObjectMapper mapper) {
        ErrorDecoder fallback = new ErrorDecoder.Default();
        return (methodKey, response) -> switch (response.status()) {
            case 404 -> new AccountNotFoundException(detail(mapper, response, "Compte introuvable"));
            case 400, 422 -> new BusinessRuleException(detail(mapper, response, "Opération refusée par account-service"));
            default -> fallback.decode(methodKey, response);
        };
    }

    private String detail(ObjectMapper mapper, Response response, String defaultMessage) {
        if (response.body() == null) {
            return defaultMessage;
        }
        try (InputStream in = response.body().asInputStream()) {
            JsonNode detail = mapper.readTree(in).get("detail");
            return detail == null ? defaultMessage : detail.asText();
        } catch (IOException ex) {
            return defaultMessage;
        }
    }
}
