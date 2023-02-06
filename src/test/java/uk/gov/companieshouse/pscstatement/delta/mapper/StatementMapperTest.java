package uk.gov.companieshouse.pscstatement.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { StatementMapperImpl.class})
public class StatementMapperTest {
    
    private ObjectMapper mapper;
    private PscStatementDelta deltaObject;
    private PscStatement pscStatement;

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
    public void shouldMapPscStatementToStatement() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Statement statement = statementMapper.pscStatementToStatement(pscStatement);
        statement.setEtag(null);
        Statement expectedResult = new Statement();

        StatementLinksType links = new StatementLinksType();
        links.setSelf("/company/08694860/persons-with-significant-control-statements/bPLPNQOiML19UP_m7WEo5sO4jS0");
        links.setPersonWithSignificantControl("/company/08694860/persons-with-significant-control/individual/Uuiit_lN49JBa-Jp3bqNLsa3UG8");

        expectedResult.setCeasedOn(LocalDate.of(2023, 1, 27));
        expectedResult.setNotificationId("3005011944");
        expectedResult.setNotifiedOn(LocalDate.of(2016, 3, 14));
        expectedResult.setKind(KindEnum.PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT);
        expectedResult.setStatement(StatementEnum.PSC_HAS_FAILED_TO_CONFIRM_CHANGED_DETAILS);
        expectedResult.setLinkedPscName("Mr Faxrivulet Congresspersonliquor");
        expectedResult.setNotificationId("Uuiit_lN49JBa-Jp3bqNLsa3UG8");
        expectedResult.setLinks(links);
        expectedResult.setRestrictionsNoticeWithdrawalReason(null);

        assertEquals(statement.getCeasedOn(), LocalDate.of(2023, 1, 27));
        assertEquals(expectedResult, statement);

    }
}
