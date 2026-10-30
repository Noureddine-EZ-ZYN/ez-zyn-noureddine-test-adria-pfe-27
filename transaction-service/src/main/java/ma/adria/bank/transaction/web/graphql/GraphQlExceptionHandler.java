package ma.adria.bank.transaction.web.graphql;

import graphql.GraphQLError;
import ma.adria.bank.transaction.service.AccountNotFoundException;
import ma.adria.bank.transaction.service.AccountServiceUnavailableException;
import ma.adria.bank.transaction.service.BusinessRuleException;
import ma.adria.bank.transaction.service.TransactionNotFoundException;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GraphQlExceptionHandler {

    @GraphQlExceptionHandler({AccountNotFoundException.class, TransactionNotFoundException.class})
    public GraphQLError handleNotFound(RuntimeException ex) {
        return error(ErrorType.NOT_FOUND, ex);
    }

    @GraphQlExceptionHandler(BusinessRuleException.class)
    public GraphQLError handleBusinessRule(BusinessRuleException ex) {
        return error(ErrorType.BAD_REQUEST, ex);
    }

    @GraphQlExceptionHandler(AccountServiceUnavailableException.class)
    public GraphQLError handleUnavailable(AccountServiceUnavailableException ex) {
        return error(ErrorType.INTERNAL_ERROR, ex);
    }

    private GraphQLError error(ErrorType type, RuntimeException ex) {
        return GraphQLError.newError().errorType(type).message(ex.getMessage()).build();
    }
}
