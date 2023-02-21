package uk.gov.companieshouse.pscstatement.delta.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsDelete;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.exception.RetryableErrorException;

@ExtendWith(MockitoExtension.class)
public class ResponseHandlerTest {

    private ResponseHandler responseHandler;

    @Mock
    private Logger logger;

    @Mock
    private PscStatementsDelete pscStatementsDelete;

    @BeforeEach
    public void setUp(){
        responseHandler = new ResponseHandler();
    }

    @Test
    public void returnOkResponseFromDataApi() throws ApiErrorResponseException, URIValidationException {
        ApiResponse<Void> expectedResponse = new ApiResponse<>(200, null, null);
        when(pscStatementsDelete.execute()).thenReturn(expectedResponse);

        ApiResponse<Void> response = responseHandler.
                handleApiResponse(logger,null,null,null,pscStatementsDelete);
        assertEquals(response, expectedResponse);
    }

    @Test
    public void throwValidationErrorResponse() throws ApiErrorResponseException, URIValidationException {
        when(pscStatementsDelete.execute()).thenThrow(new URIValidationException("invalid path"));

        RetryableErrorException thrown = assertThrows(RetryableErrorException.class, ()-> {
            responseHandler.
                    handleApiResponse(logger, null,null,null, pscStatementsDelete);
        });
        assertEquals("Invalid path specified", thrown.getMessage());
    }

    @Test
    public void throwApiErrorResponseOn400() throws ApiErrorResponseException, URIValidationException {
        HttpResponseException.Builder builder = new HttpResponseException.Builder(400,
                "BAD_REQUEST",new HttpHeaders());
        when(pscStatementsDelete.execute()).thenThrow(new ApiErrorResponseException(builder));

        NonRetryableErrorException thrown = assertThrows(NonRetryableErrorException.class, ()-> {
            responseHandler.
                    handleApiResponse(logger, null,null,null, pscStatementsDelete);
        });
        assertEquals("400 BAD_REQUEST response received from psc-statements-data-api", thrown.getMessage());
    }

    @Test
    public void throwErrorResponseOn404() throws ApiErrorResponseException, URIValidationException {
        HttpResponseException.Builder builder = new HttpResponseException.Builder(404,
                "server error",new HttpHeaders());
        when(pscStatementsDelete.execute()).thenThrow(new ApiErrorResponseException(builder));

        RetryableErrorException thrown = assertThrows(RetryableErrorException.class, ()-> {
            responseHandler.
                    handleApiResponse(logger, null,null,null, pscStatementsDelete);
        });
        assertEquals("server error with 404 NOT_FOUND returned from psc-statements-data-api", thrown.getMessage());
    }

    @Test
    public void throwErrorResponseOn500() {
        ResponseHandler spyHandler = spy(responseHandler);
        doThrow(RetryableErrorException.class).when(spyHandler).handleApiResponse(logger, null,
                null, null, pscStatementsDelete);

        RetryableErrorException thrown = assertThrows(RetryableErrorException.class, ()-> {
            spyHandler.
                    handleApiResponse(logger, null,null,null, pscStatementsDelete);
        });
    }
}