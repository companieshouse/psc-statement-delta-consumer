package uk.gov.companieshouse.pscstatement.delta.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsDelete;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsPut;

@Component
public class ResponseHandlerFactory {

    /**
     * return the required response handler based on the executor to be parameterised.
     *
     * @param resourceHandler the child of the executor
     * @return parameterised response handler
     */
    public Object createResponseHandler(Object resourceHandler) {
        if (resourceHandler instanceof PscStatementsPut) {
            return new ResponseHandler<PscStatementsPut>();
        }
        return new ResponseHandler<PscStatementsDelete>();
    }
}