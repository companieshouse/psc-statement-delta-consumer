package uk.gov.companieshouse.pscstatement.delta.processor;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;


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
    public void processDelta(Message<ChsDelta> chsDelta) {
        final MessageHeaders headers = chsDelta.getHeaders();
        final ChsDelta payload = chsDelta.getPayload();
        logger.info(format("Successfully extracted Chs Delta of %s", payload));
        ObjectMapper mapper = new ObjectMapper();
        PscStatementDelta pscStatementDelta;
        try {
            pscStatementDelta = mapper.readValue(payload.getData(),
                    PscStatementDelta.class);
            logger.info(format("Successfully extracted psc-statement delta of %s",
                    pscStatementDelta.toString()));
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting psc-statement delta", ex);
        }
    }
}
