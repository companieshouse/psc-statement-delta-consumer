package uk.gov.companieshouse.pscstatement.delta.processor;

import static java.lang.String.format;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.transformer.PscStatementTransformer;


@Component
public class PscStatementDeltaProcessor {

    private final Logger logger;
    private final PscStatementTransformer transformer;

    @Autowired
    public PscStatementDeltaProcessor(Logger logger, PscStatementTransformer transformer) {
        this.logger = logger;
        this.transformer = transformer;
    }

    /**
     * Process PSC Statement Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
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
        List<PscStatement> pscStatements = pscStatementDelta.getPscStatements();
        if (pscStatements.isEmpty()) {
            throw new NonRetryableErrorException("empty list statements provided");
        } else {
            for (PscStatement pscStatement : pscStatements) {
                CompanyPscStatement companyPscStatement = transformer.transform(pscStatement);
                companyPscStatement.setDeltaAt(pscStatementDelta.getDeltaAt());
            }
        }
    }
}
