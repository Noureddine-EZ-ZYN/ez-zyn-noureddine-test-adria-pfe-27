package ma.adria.bank.assistant.web.rest;

import ma.adria.bank.assistant.service.AgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(AgentException.class)
    public ProblemDetail handleAgentFailure(AgentException ex) {
        log.error("Échec de l'agent", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "L'assistant n'a pas pu traiter la demande : " + ex.getMessage());
    }
}
