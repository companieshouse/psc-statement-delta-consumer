package uk.gov.companieshouse.pscstatement.delta.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsDelete;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.exception.RetryableErrorException;

@Service
public class ResponseHandler {

    /**
     * Handle response from data api.
     */
    public ApiResponse<Void> handleApiResponse(Logger logger, String context, String operation,
                                               String uri, PscStatementsDelete delete) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("operation_name", operation);
        logMap.put("path", uri);

        try {
            return delete.execute();

        } catch (URIValidationException ex) {
            String msg = "Invalid path specified";
            logger.errorContext(context, msg, ex, logMap);

            throw new RetryableErrorException(msg, ex);
        } catch (ApiErrorResponseException ex) {

            if (ex.getStatusCode() == 400) {
                // 400 bad request cannot be retried
                String msg = "400 BAD_REQUEST response received from psc-statements-data-api";
                logMap.put("status", ex.getStatusCode());
                logger.errorContext(context, msg, ex, logMap);
                throw new NonRetryableErrorException(msg, ex);
            } else if (ex.getStatusCode() == 404) {
                String msg = "server error with 404 NOT_FOUND returned from psc-statements-data-api";
                throw new RetryableErrorException(msg, ex);
            }
            String msg = "Unsuccessful response received from psc-statements-data-api";
            logger.errorContext(context, msg, ex, logMap);
            throw new RetryableErrorException(ex);
        } catch (Exception ex) {
            String msg = "error response";
            logger.errorContext(context, msg, ex, logMap);
            throw new RetryableErrorException(ex);
        }
    }
}
