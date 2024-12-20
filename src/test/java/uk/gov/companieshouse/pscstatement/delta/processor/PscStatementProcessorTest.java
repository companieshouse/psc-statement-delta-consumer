package uk.gov.companieshouse.pscstatement.delta.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import consumer.exception.RetryableErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.mapper.MapperUtils;
import uk.gov.companieshouse.pscstatement.delta.service.ApiClientService;
import uk.gov.companieshouse.pscstatement.delta.transformer.PscStatementApiTransformer;
import uk.gov.companieshouse.pscstatement.delta.utils.TestHelper;


@ExtendWith(MockitoExtension.class)
class PscStatementProcessorTest {

    private static final String PSC_STATEMENT_ID_RAW = "3000000002";
    private static final String COMPANY_NUMBER = "09950914";

    private final TestHelper testHelper = new TestHelper();
    CompanyPscStatement mockCompanyPscStatement;
    private PscStatementDeltaProcessor deltaProcessor;
    @Mock
    private Logger logger;
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private PscStatementApiTransformer transformer;
    private MapperUtils mapperUtils;

    @BeforeEach
    void setUp() {
        deltaProcessor = new PscStatementDeltaProcessor(logger, apiClientService, transformer);
        mapperUtils = new MapperUtils();
        ReflectionTestUtils.setField(deltaProcessor, "mapperUtils", mapperUtils);

    }

    @Test
    void When_InvalidChsDeltaMessage_Expect_RetryableError() {
        // given
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();

        // when
        Executable actual = () -> deltaProcessor.processDelta(mockChsDeltaMessage);

        // then
        Assertions.assertThrows(RetryableErrorException.class, actual);
        Mockito.verifyNoInteractions(apiClientService);
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid ChsDelta is given")
    void When_ValidChsDeltaMessage_Expect_ProcessorDoesNotThrow_CallsTransformer()
            throws IOException {
        // given
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(false);
        mockCompanyPscStatement = new CompanyPscStatement();
        mockCompanyPscStatement.setPscStatementIdRaw(PSC_STATEMENT_ID_RAW);
        mockCompanyPscStatement.setCompanyNumber(COMPANY_NUMBER);
        String pscStatementIdEncoded = mapperUtils.encode(PSC_STATEMENT_ID_RAW);
        when(transformer.transform(any(PscStatement.class))).thenReturn(mockCompanyPscStatement);

        // when
        deltaProcessor.processDelta(mockChsDeltaMessage);

        // then
        verify(transformer).transform(any(PscStatement.class));
        Mockito.verify(apiClientService).
                invokePscStatementPutRequest(COMPANY_NUMBER, pscStatementIdEncoded, mockCompanyPscStatement);

    }

    @Test
    void When_InvalidChsDeleteDeltaMessage_Expect_RetryableError() {
        // given
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();

        // when
        Executable actual = () -> deltaProcessor.processDeleteDelta(mockChsDeltaMessage);

        // given
        Assertions.assertThrows(RetryableErrorException.class, actual);
        Mockito.verifyNoInteractions(apiClientService);
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid delete ChsDelta is given")
    void When_ValidChsDeleteDeltaMessage_Expect_ProcessorDoesNotThrow() throws IOException {
        // given
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(true);
        String pscStatementIdEncoded = mapperUtils.encode(PSC_STATEMENT_ID_RAW);

        // when
        deltaProcessor.processDeleteDelta(mockChsDeltaMessage);

        // then
        Mockito.verify(apiClientService).
                invokePscStatementDeleteRequest(COMPANY_NUMBER, pscStatementIdEncoded, "20230724093435661593");
    }
}
