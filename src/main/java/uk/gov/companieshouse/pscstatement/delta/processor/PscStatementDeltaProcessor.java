package uk.gov.companieshouse.pscstatement.delta.processor;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;

import consumer.exception.NonRetryableErrorException;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.delta.PscStatementDeleteDelta;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatement.delta.mapper.MapperUtils;
import uk.gov.companieshouse.pscstatement.delta.service.ApiClientService;
import uk.gov.companieshouse.pscstatement.delta.transformer.PscStatementApiTransformer;

@Component
public class PscStatementDeltaProcessor {

    private final PscStatementApiTransformer transformer;
    private final Logger logger;

    private ApiClientService apiClientService;

    @Autowired
    private MapperUtils mapperUtils;

    /**
     * processor constructor.
     */
    @Autowired
    public PscStatementDeltaProcessor(Logger logger, ApiClientService apiClientService,
                                      PscStatementApiTransformer transformer) {
        this.logger = logger;
        this.apiClientService = apiClientService;
        this.transformer = transformer;
    }

    /**
     * Process PSC Statement Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();

        CompanyPscStatement companyPscStatement = new CompanyPscStatement();


        ObjectMapper mapper = new ObjectMapper();
        PscStatementDelta pscStatementDelta;
        try {
            pscStatementDelta = mapper.readValue(payload.getData(),
                    PscStatementDelta.class);
            logger.trace(format("Successfully extracted psc-statement delta of %s",
                    pscStatementDelta.toString()));
            List<PscStatement> statements = pscStatementDelta.getPscStatements();
            for (PscStatement pscStatement : statements) {
                companyPscStatement = transformer.transform(pscStatement);
                companyPscStatement.setDeltaAt(pscStatementDelta.getDeltaAt());
                final String companyNumber = companyPscStatement.getCompanyNumber();
                DataMapHolder.get()
                        .companyNumber(companyNumber);
                logger.infoContext(contextId,String.format("Successfully extracted Chs Delta with contextId %s",
                                contextId),
                                DataMapHolder.getLogMap());
            }
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting psc-statement delta", ex);
        }

        apiClientService.invokePscStatementPutHandler(contextId, companyPscStatement.getCompanyNumber(),
                companyPscStatement.getPscStatementId(), companyPscStatement);
    }

    /**
     * Process PSC Statement delete Delta message.
     */
    public void processDeleteDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();
        PscStatementDeleteDelta pscStatementDeleteDelta;

        ObjectMapper mapper = new ObjectMapper();
        try {
            pscStatementDeleteDelta = mapper.readValue(payload.getData(), PscStatementDeleteDelta.class);
            final String companyNumber = pscStatementDeleteDelta.getCompanyNumber();
            DataMapHolder.get()
                    .companyNumber(companyNumber);
            logger.infoContext(contextId,String.format("Successfully extracted Chs Delta with contextId %s",
                            contextId),
                            DataMapHolder.getLogMap());
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting psc-statement delete delta", ex);
        }
        final String statementId = mapperUtils.encode(pscStatementDeleteDelta.getPscStatementId());
        apiClientService.invokePscStatementDeleteHandler(contextId, pscStatementDeleteDelta.getCompanyNumber(),
                statementId);
    }
}

