package uk.gov.companieshouse.pscstatement.delta.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsDelete;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsPut;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceTest {

    private static final String COMPANY_NUMBER = "company_number";
    private static final String STATEMENT_ID = "statement_id";
    private static final String DELTA_AT = "20240219123045999999";
    private static final String URI = "/company/%s/persons-with-significant-control-statements/%s/internal";
    private static final ApiResponse<Void> SUCCESS_RESPONSE = new ApiResponse<>(200, null);

    @InjectMocks
    private ApiClientService apiClientService;

    @Mock
    private ResponseHandler responseHandler;
    @Mock
    private Supplier<InternalApiClient> internalApiClientSupplier;

    @Mock
    private CompanyPscStatement companyPscStatement;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PscStatementsPut pscStatementsPut;
    @Mock
    private PscStatementsDelete pscStatementsDelete;

    @Test
    void shouldSuccessfullySendPutRequestToApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putPscStatements(anyString(), any(CompanyPscStatement.class))).thenReturn(
                pscStatementsPut);
        when(pscStatementsPut.execute()).thenReturn(SUCCESS_RESPONSE);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, STATEMENT_ID);

        // when
        apiClientService.invokePscStatementPutRequest(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        // then
        verify(privateDeltaResourceHandler).putPscStatements(formattedUri, companyPscStatement);
        verifyNoMoreInteractions(responseHandler);
    }

    @Test
    void shouldSendPutRequestAndHandleNon200ResponseFromApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putPscStatements(anyString(), any(CompanyPscStatement.class))).thenReturn(
                pscStatementsPut);
        when(pscStatementsPut.execute()).thenThrow(ApiErrorResponseException.class);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, STATEMENT_ID);

        // when
        apiClientService.invokePscStatementPutRequest(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        // then
        verify(privateDeltaResourceHandler).putPscStatements(formattedUri, companyPscStatement);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
    }

    @Test
    void shouldSendPutRequestAndHandleURIValidationExceptionFromApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putPscStatements(anyString(), any(CompanyPscStatement.class))).thenReturn(
                pscStatementsPut);
        when(pscStatementsPut.execute()).thenThrow(URIValidationException.class);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, STATEMENT_ID);

        // when
        apiClientService.invokePscStatementPutRequest(COMPANY_NUMBER, STATEMENT_ID, companyPscStatement);

        // then
        verify(privateDeltaResourceHandler).putPscStatements(formattedUri, companyPscStatement);
        verify(responseHandler).handle(any(URIValidationException.class));
    }

    @Test
    void shouldSuccessfullySendDeleteRequestToApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deletePscStatements(anyString(), anyString())).thenReturn(pscStatementsDelete);
        when(pscStatementsDelete.execute()).thenReturn(SUCCESS_RESPONSE);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, STATEMENT_ID);

        // when
        apiClientService.invokePscStatementDeleteRequest(COMPANY_NUMBER, STATEMENT_ID, DELTA_AT);

        // then
        verify(privateDeltaResourceHandler).deletePscStatements(formattedUri, DELTA_AT);
        verifyNoMoreInteractions(responseHandler);
    }

    @Test
    void shouldSendDeleteRequestAndHandleNon200ResponseFromApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deletePscStatements(anyString(), anyString())).thenReturn(pscStatementsDelete);
        when(pscStatementsDelete.execute()).thenThrow(ApiErrorResponseException.class);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, STATEMENT_ID);

        // when
        apiClientService.invokePscStatementDeleteRequest(COMPANY_NUMBER, STATEMENT_ID, DELTA_AT);

        // then
        verify(privateDeltaResourceHandler).deletePscStatements(formattedUri, DELTA_AT);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
    }

    @Test
    void shouldSendDeleteRequestAndHandleURIValidationExceptionFromApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deletePscStatements(anyString(), anyString())).thenReturn(pscStatementsDelete);
        when(pscStatementsDelete.execute()).thenThrow(URIValidationException.class);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, STATEMENT_ID);

        // when
        apiClientService.invokePscStatementDeleteRequest(COMPANY_NUMBER, STATEMENT_ID, DELTA_AT);

        // then
        verify(privateDeltaResourceHandler).deletePscStatements(formattedUri, DELTA_AT);
        verify(responseHandler).handle(any(URIValidationException.class));
    }
}


