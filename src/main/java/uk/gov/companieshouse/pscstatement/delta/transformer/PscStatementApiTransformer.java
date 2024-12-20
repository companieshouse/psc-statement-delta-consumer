package uk.gov.companieshouse.pscstatement.delta.transformer;

import consumer.exception.NonRetryableErrorException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatement.delta.mapper.StatementMapper;


@Component
public class PscStatementApiTransformer {

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
        } catch (Exception exception) {
            throw new NonRetryableErrorException(exception);
        }
    }
}
