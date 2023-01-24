package uk.gov.companieshouse.pscstatement.delta.transformer;

import org.springframework.beans.factory.annotation.Autowired;

import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatement.delta.mapper.MapperUtils;
import uk.gov.companieshouse.pscstatement.delta.mapper.StatementMapper;

public class PscStatementTransformer {

    private final StatementMapper statementMapper;

    /**
     * Constructor for the transformer.
     * @param statementMapper returns the api object.
     */
    @Autowired
    public PscStatementTransformer (StatementMapper statementMapper) {
        this.statementMapper = statementMapper;
    }

    public CompanyPscStatement transform(PscStatement pscStatement) {
        Statement statement = statementMapper.pscStatementToStatement(pscStatement);
        CompanyPscStatement companyPscStatement = new CompanyPscStatement();
        companyPscStatement.setStatement(statement);
        companyPscStatement.setCompanyNumber(pscStatement.getCompanyNumber());
        companyPscStatement.setPscStatementId(MapperUtils.encode(pscStatement.getPscStatementId()));
        return companyPscStatement;
    }
    
}
