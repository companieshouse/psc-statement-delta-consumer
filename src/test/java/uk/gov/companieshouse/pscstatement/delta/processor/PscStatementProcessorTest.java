package uk.gov.companieshouse.pscstatement.delta.processor;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import consumer.exception.RetryableErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private final TestHelper testHelper = new TestHelper();
    @Mock
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
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();
        assertThrows(RetryableErrorException.class,
                () -> deltaProcessor.processDelta(mockChsDeltaMessage));
        Mockito.verify(apiClientService, times(0)).
                invokePscStatementPutRequest(any(), any(), any());
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid ChsDelta is given")
    void When_ValidChsDeltaMessage_Expect_ProcessorDoesNotThrow_CallsTransformer()
            throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(false);
        when(transformer.transform(any(PscStatement.class))).thenReturn(mockCompanyPscStatement);
        Assertions.assertDoesNotThrow(() -> deltaProcessor.processDelta(mockChsDeltaMessage));
        verify(transformer).transform(any(PscStatement.class));
        Mockito.verify(apiClientService, times(1)).
                invokePscStatementPutRequest(any(), any(), any());

    }

    @Test
    void When_InvalidChsDeleteDeltaMessage_Expect_RetryableError() {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();
        assertThrows(RetryableErrorException.class,
                () -> deltaProcessor.processDeleteDelta(mockChsDeltaMessage));
        Mockito.verify(apiClientService, times(0)).
                invokePscStatementDeleteRequest(any(), any(), any());
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid delete ChsDelta is given")
    void When_ValidChsDeleteDeltaMessage_Expect_ProcessorDoesNotThrow() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(true);
        Assertions.assertDoesNotThrow(() -> deltaProcessor.processDeleteDelta(mockChsDeltaMessage));
        Mockito.verify(apiClientService, times(1)).
                invokePscStatementDeleteRequest(any(), any(), any());
    }
}
