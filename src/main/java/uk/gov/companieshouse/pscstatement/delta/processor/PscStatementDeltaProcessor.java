package uk.gov.companieshouse.pscstatement.delta.processor;

import static java.lang.String.format;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;


@Component
public class PscStatementDeltaProcessor {

    private final Logger logger;

    @Autowired
    public PscStatementDeltaProcessor(Logger logger) {
        this.logger = logger;
    }

    /**
     * Process PSC Statement Delta message.
     */
    public void processDelta(Message<PscStatementDelta> pscStatementDelta) {
        final MessageHeaders headers = pscStatementDelta.getHeaders();
        final PscStatementDelta payload = pscStatementDelta.getPayload();
        logger.info(format("Successfully extracted PSC Statement Delta of %s", payload.toString()));
    }
}
