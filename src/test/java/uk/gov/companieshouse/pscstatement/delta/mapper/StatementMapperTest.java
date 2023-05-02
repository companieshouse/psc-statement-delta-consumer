package uk.gov.companieshouse.pscstatement.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementLinksType;
import uk.gov.companieshouse.api.psc.Statement.KindEnum;
import uk.gov.companieshouse.api.psc.Statement.StatementEnum;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = { StatementMapperImpl.class})
public class StatementMapperTest {
    
    private ObjectMapper mapper;
    private PscStatementDelta deltaObject;
    private PscStatement pscStatement;


    @MockBean
    private MapperUtils mapperUtils;
    @Autowired
    StatementMapper statementMapper;

    @BeforeEach
    public void setUp() throws Exception {
        mapper = new ObjectMapper();

        String path = "psc-statement-delta-example.json";
        String input = FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        deltaObject = mapper.readValue(input, PscStatementDelta.class);
        pscStatement = deltaObject.getPscStatements().get(0);

    }

    @Test
    public void shouldMapPscStatementToStatement() {
        when(mapperUtils.encode("3000000002")).thenReturn("I5tVa-U7URp5pDuXSyEQ8NILVWU");
        when(mapperUtils.encode("3005011944")).thenReturn("Uuiit_lN49JBa-Jp3bqNLsa3UG8");
        Statement statement = statementMapper.pscStatementToStatement(pscStatement);
        statement.setEtag(null);
        Statement expectedResult = new Statement();

        StatementLinksType links = new StatementLinksType();
        links.setSelf("/company/08694860/persons-with-significant-control-statements/I5tVa-U7URp5pDuXSyEQ8NILVWU");
        links.setPersonWithSignificantControl("/company/08694860/persons-with-significant-control/individual/Uuiit_lN49JBa-Jp3bqNLsa3UG8");

        expectedResult.setCeasedOn(LocalDate.of(2023, 1, 27));
        expectedResult.setNotifiedOn(LocalDate.of(2016, 3, 14));
        expectedResult.setKind(KindEnum.PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT);
        expectedResult.setStatement(StatementEnum.PSC_HAS_FAILED_TO_CONFIRM_CHANGED_DETAILS);
        expectedResult.setLinkedPscName("Mr Faxrivulet Congresspersonliquor");
        expectedResult.setLinks(links);
        expectedResult.setRestrictionsNoticeWithdrawalReason("restrictions-notice-withdrawn-by-company");

        assertEquals(statement.getCeasedOn(), LocalDate.of(2023, 1, 27));
        assertEquals(expectedResult, statement);

    }
}
