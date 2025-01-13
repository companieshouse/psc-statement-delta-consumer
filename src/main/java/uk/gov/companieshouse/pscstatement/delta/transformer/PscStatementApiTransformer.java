package uk.gov.companieshouse.pscstatement.delta.transformer;

import static uk.gov.companieshouse.pscstatement.delta.PscStatementDeltaConsumerApplication.APPLICATION_NAME_SPACE;

import consumer.exception.NonRetryableErrorException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatement.delta.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatement.delta.mapper.StatementMapper;


@Component
public class PscStatementApiTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private static final String TRANSFORM_ERROR_MESSAGE = "Error transforming PSC statement";

    private final StatementMapper mapper;

    /**
     * constructor.
     */
    public PscStatementApiTransformer(StatementMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * turns pscStatement into CompanyPscStatement.
     * @throws NonRetryableErrorException Throws when transformation is non retryable
     */
    public CompanyPscStatement transform(PscStatement pscStatement) {
        try {
            Statement statement = mapper.pscStatementToStatement(pscStatement);
            CompanyPscStatement companyPscStatement = new CompanyPscStatement();
            companyPscStatement.setCompanyNumber(pscStatement.getCompanyNumber());
            companyPscStatement.setPscStatementIdRaw(pscStatement.getPscStatementId());
            companyPscStatement.setStatement(statement);
            return companyPscStatement;
        } catch (Exception ex) {
            LOGGER.error(TRANSFORM_ERROR_MESSAGE, ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(TRANSFORM_ERROR_MESSAGE, ex);
        }
    }
}
