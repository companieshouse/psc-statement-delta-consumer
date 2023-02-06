package uk.gov.companieshouse.pscstatement.delta.processor;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.transformer.PscStatementApiTransformer;


@Component
public class PscStatementDeltaProcessor {

    private final PscStatementApiTransformer transformer;
    private final Logger logger;

    @Autowired
    public PscStatementDeltaProcessor(PscStatementApiTransformer transformer, Logger logger) {
        this.logger = logger;
        this.transformer = transformer;
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
            logger.trace(format("Successfully extracted psc-statement delta of %s",
                    pscStatementDelta.toString()));
            List<PscStatement> statements = pscStatementDelta.getPscStatements();
            for (PscStatement pscStatement : statements) {
                CompanyPscStatement companyPscStatement = transformer.transform(pscStatement);
                companyPscStatement.setDeltaAt(pscStatementDelta.getDeltaAt());
                logger.info(format("CompanyPscStatement: %s", companyPscStatement)); //remove
                
            }
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting psc-statement delta", ex);
        }
    }
}
