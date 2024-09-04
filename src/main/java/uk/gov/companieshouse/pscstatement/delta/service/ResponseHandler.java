package uk.gov.companieshouse.pscstatement.delta.service;

import consumer.exception.NonRetryableErrorException;
import consumer.exception.RetryableErrorException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.Executor;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

@SuppressWarnings("unchecked")
@Service
public class ResponseHandler<T> {

    /**
     * Handle response from data api.
     *
     * @throws NonRetryableErrorException Throws when transformation is non retryable
     * @throws RetryableErrorException Throws when transformation is retryable
     */
    public ApiResponse<Void> handleApiResponse(Logger logger, String context, String operation,
            String uri, T executor) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("operation_name", operation);
        logMap.put("path", uri);

        try {
            return (ApiResponse<Void>) ((Executor<T>) executor).execute();

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
                String msg = "server error with 404 NOT_FOUND returned "
                        + "from psc-statements-data-api";
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
