package uk.gov.companieshouse.pscstatement.delta.processor;

import static uk.gov.companieshouse.pscstatement.delta.PscStatementDeltaConsumerApplication.APPLICATION_NAME_SPACE;

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
import uk.gov.companieshouse.pscstatement.delta.transformer.PscStatementDeltaDeserialiser;

@Component
public class PscStatementDeltaProcessor {
    private final PscStatementApiTransformer transformer;
    private final PscStatementDeltaDeserialiser deltaDeserialiser;
    private final ApiClientService apiClientService;
    private final MapperUtils mapperUtils;

    /**
     * processor constructor.
     */
    public PscStatementDeltaProcessor(PscStatementApiTransformer transformer,
            PscStatementDeltaDeserialiser deltaDeserialiser, ApiClientService apiClientService,
            MapperUtils mapperUtils) {
        this.transformer = transformer;
        this.deltaDeserialiser = deltaDeserialiser;
        this.apiClientService = apiClientService;
        this.mapperUtils = mapperUtils;
    }

    /**
     * Process PSC Statement Delta message.
     * @throws RetryableErrorException when transformation is retryable
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        CompanyPscStatement companyPscStatement = new CompanyPscStatement();
        PscStatementDelta pscStatementDelta = deltaDeserialiser.deserialisePscStatementDelta(payload.getData());
        List<PscStatement> statements = pscStatementDelta.getPscStatements();
        for (PscStatement pscStatement : statements) {
            DataMapHolder.get().companyNumber(pscStatement.getCompanyNumber())
                    .pscStatementIdRaw(pscStatement.getPscStatementId());
            companyPscStatement = transformer.transform(pscStatement);
            companyPscStatement.setDeltaAt(pscStatementDelta.getDeltaAt());
        }
        final String statementId = mapperUtils.encode(companyPscStatement.getPscStatementIdRaw());
        DataMapHolder.get().pscStatementId(statementId);
        apiClientService.invokePscStatementPutRequest(companyPscStatement.getCompanyNumber(), statementId,
                companyPscStatement);
    }

    /**
     * Process PSC Statement delete Delta message.
     * @throws RetryableErrorException when transformation is retryable
     */
    public void processDeleteDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        PscStatementDeleteDelta pscStatementDeleteDelta = deltaDeserialiser.deserialisePscStatementDeleteDelta(
                payload.getData());
        String companyNumber = pscStatementDeleteDelta.getCompanyNumber();
        DataMapHolder.get().companyNumber(companyNumber)
                .pscStatementIdRaw(pscStatementDeleteDelta.getPscStatementId());

        String statementId = mapperUtils.encode(pscStatementDeleteDelta.getPscStatementId());
        DataMapHolder.get().pscStatementId(statementId);
        apiClientService.invokePscStatementDeleteRequest(companyNumber, statementId, pscStatementDeleteDelta.getDeltaAt());
    }
}

