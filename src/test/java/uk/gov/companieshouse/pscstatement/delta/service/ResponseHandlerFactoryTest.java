package uk.gov.companieshouse.pscstatement.delta.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsDelete;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsPut;

public class ResponseHandlerFactoryTest {

    ResponseHandlerFactory responseHandlerFactory;

    @BeforeEach
    void setUp() {
        responseHandlerFactory = new ResponseHandlerFactory();
    }

    @Test
    void putExecuteOpReturnsResponseHandler() {
        PscStatementsPut putExecutor = new PscStatementsPut(null, null, null, null, null);

        assert (responseHandlerFactory.createResponseHandler(
                putExecutor) instanceof ResponseHandler<?>);
    }

    @Test
    void deleteExecuteOpReturnsResponseHandler() {
        PscStatementsDelete deleteExecutor = new PscStatementsDelete(null, null, null, null);

        assert (responseHandlerFactory.createResponseHandler(
                deleteExecutor) instanceof ResponseHandler<?>);
    }
}
