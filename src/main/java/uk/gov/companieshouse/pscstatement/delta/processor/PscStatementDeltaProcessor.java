package uk.gov.companieshouse.pscstatement.delta.processor;

import static java.lang.String.format;
import static uk.gov.companieshouse.pscstatement.delta.PscStatementDeltaConsumerApplication.APPLICATION_NAME_SPACE;

import com.fasterxml.jackson.databind.ObjectMapper;
import consumer.exception.RetryableErrorException;
import java.util.List;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.delta.PscStatementDeleteDelta;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatement.delta.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatement.delta.mapper.MapperUtils;
import uk.gov.companieshouse.pscstatement.delta.service.ApiClientService;
import uk.gov.companieshouse.pscstatement.delta.transformer.PscStatementApiTransformer;

@Component
public class PscStatementDeltaProcessor {

    private static final Logger logger = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final PscStatementApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final MapperUtils mapperUtils;

    /**
     * processor constructor.
     */
    public PscStatementDeltaProcessor(PscStatementApiTransformer transformer, ApiClientService apiClientService,
            MapperUtils mapperUtils) {
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.mapperUtils = mapperUtils;
    }

    /**
     * Process PSC Statement Delta message.
     * @throws RetryableErrorException when transformation is retryable
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
                logger.infoContext(contextId,
                        String.format("Successfully extracted Chs Delta with contextId %s",
                                contextId),
                        DataMapHolder.getLogMap());
            }
        } catch (Exception ex) {
            throw new RetryableErrorException(
                    "Error when extracting psc-statement delta", ex);
        }

        final String statementId = mapperUtils.encode(companyPscStatement.getPscStatementIdRaw());
        apiClientService.invokePscStatementPutRequest(companyPscStatement.getCompanyNumber(),
                statementId, companyPscStatement);
    }

    /**
     * Process PSC Statement delete Delta message.
     * @throws RetryableErrorException when transformation is retryable
     */
    public void processDeleteDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();
        PscStatementDeleteDelta pscStatementDeleteDelta;

        ObjectMapper mapper = new ObjectMapper();
        try {
            pscStatementDeleteDelta = mapper.readValue(payload.getData(),
                    PscStatementDeleteDelta.class);
            final String companyNumber = pscStatementDeleteDelta.getCompanyNumber();
            DataMapHolder.get()
                    .companyNumber(companyNumber);
            logger.infoContext(contextId,
                    String.format("Successfully extracted Chs Delta with contextId %s",
                            contextId),
                    DataMapHolder.getLogMap());
        } catch (Exception ex) {
            throw new RetryableErrorException(
                    "Error when extracting psc-statement delete delta", ex);
        }
        final String statementId = mapperUtils.encode(pscStatementDeleteDelta.getPscStatementId());
        apiClientService.invokePscStatementDeleteRequest(pscStatementDeleteDelta.getCompanyNumber(),
                statementId, pscStatementDeleteDelta.getDeltaAt());
    }
}

