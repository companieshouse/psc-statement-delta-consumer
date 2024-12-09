package uk.gov.companieshouse.pscstatement.delta.service;

import static uk.gov.companieshouse.pscstatement.delta.PscStatementDeltaConsumerApplication.APPLICATION_NAME_SPACE;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatement.delta.logging.DataMapHolder;

@Component
public class ApiClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private static final String URI = "/company/%s/persons-with-significant-control-statements/%s/internal";

    private final ResponseHandler responseHandler;
    private final Supplier<InternalApiClient> internalApiClientSupplier;

    public ApiClientService(ResponseHandler responseHandler, Supplier<InternalApiClient> internalApiClientSupplier) {
        this.responseHandler = responseHandler;
        this.internalApiClientSupplier = internalApiClientSupplier;
    }

    public void invokePscStatementPutRequest(String companyNumber, String statementId, CompanyPscStatement statement) {
        final String formattedUri = URI.formatted(companyNumber, statementId);
        try {
            internalApiClientSupplier.get()
                    .privateDeltaResourceHandler()
                    .putPscStatements(formattedUri, statement)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
        LOGGER.info("PUT request successfully sent to API", DataMapHolder.getLogMap());
    }

    public void invokePscStatementDeleteRequest(String companyNumber, String statementId, String deltaAt) {
        final String formattedUri = URI.formatted(companyNumber, statementId);
        try {
            internalApiClientSupplier.get()
                    .privateDeltaResourceHandler()
                    .deletePscStatements(formattedUri, deltaAt)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
        LOGGER.info("DELETE request successfully sent to API", DataMapHolder.getLogMap());
    }
}

