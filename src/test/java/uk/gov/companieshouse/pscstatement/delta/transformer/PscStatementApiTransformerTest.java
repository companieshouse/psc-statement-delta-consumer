package uk.gov.companieshouse.pscstatement.delta.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import consumer.exception.NonRetryableErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.pscstatement.delta.mapper.MapperUtils;
import uk.gov.companieshouse.pscstatement.delta.mapper.StatementMapper;

@ExtendWith(SpringExtension.class)
public class PscStatementApiTransformerTest {

    @Mock
    private StatementMapper mapper;

    private PscStatementApiTransformer transformer;
    private PscStatement pscStatement;
    private MapperUtils mapperUtils;

    @BeforeEach
    public void setUp() {
        transformer = new PscStatementApiTransformer(mapper);
        pscStatement = new PscStatement();
        mapperUtils = new MapperUtils();
        ReflectionTestUtils.setField(transformer, "mapperUtils", mapperUtils);
    }

    @Test
    public void transformerReturnsCompanyPscStatement() {
        Statement mockStatement = mock(Statement.class);
        when(mapper.pscStatementToStatement(pscStatement)).thenReturn(mockStatement);
        CompanyPscStatement actualCompanyPscStatement = transformer.transform(pscStatement);

        CompanyPscStatement expectedCPS = new CompanyPscStatement();
        expectedCPS.setStatement(mockStatement);
        expectedCPS.setPscStatementId("yPf-Ow5BvoRtVodZLPIBj_biJoc");

        assertEquals(expectedCPS, actualCompanyPscStatement);
    }

    @Test
    public void transformerThrowsExceptionCompanyPscStatement() {
        when(mapper.pscStatementToStatement(pscStatement)).thenThrow(
                NonRetryableErrorException.class);

        assertThrows(NonRetryableErrorException.class, () ->
                transformer.transform(pscStatement));
    }
}
