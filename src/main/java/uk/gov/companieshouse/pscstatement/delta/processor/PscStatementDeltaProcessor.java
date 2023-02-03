package uk.gov.companieshouse.pscstatement.delta.processor;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatementDeleteDelta;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.service.ApiClientService;

@Component
public class PscStatementDeltaProcessor {

    private final Logger logger;

    private ApiClientService apiClientService;

    @Autowired
    public PscStatementDeltaProcessor(Logger logger, ApiClientService apiClientService) {
        this.logger = logger;
        this.apiClientService = apiClientService;
    }

    /**
     * Process PSC Statement Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        final MessageHeaders headers = chsDelta.getHeaders();
        final ChsDelta payload = chsDelta.getPayload();
        logger.info(format("Successfully extracted Chs Delta with context_id %s",
                payload.getContextId()));
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

    /**
     * Process PSC Statement delete Delta message.
     */
    public void processDeleteDelta(Message<ChsDelta> chsDelta) throws IOException, URIValidationException {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();
        PscStatementDeleteDelta pscStatementDeleteDelta;
        logger.info(String.format("Successfully extracted Chs Delta with context_id %s", payload.getContextId()));
        ObjectMapper mapper = new ObjectMapper();
        try {
            pscStatementDeleteDelta = mapper.readValue(payload.getData(), PscStatementDeleteDelta.class);
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting psc-statement delete delta", ex);
        }
        apiClientService.invokePscStatementDeleteHandler(contextId, pscStatementDeleteDelta.getCompanyNumber(),
                pscStatementDeleteDelta.getPscStatementId());

    }
}
