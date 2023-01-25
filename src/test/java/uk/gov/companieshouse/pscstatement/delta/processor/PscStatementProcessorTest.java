package uk.gov.companieshouse.pscstatement.delta.processor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.utils.TestHelper;

import java.io.IOException;

import static org.junit.Assert.*;

@ExtendWith(MockitoExtension.class)
public class PscStatementProcessorTest {
    private TestHelper testHelper = new TestHelper();

    private PscStatementDeltaProcessor deltaProcessor;
    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        deltaProcessor = new PscStatementDeltaProcessor(logger);
    }

    @Test
    void When_InvalidChsDeltaMessage_Expect_NonRetryableError() {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();
        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelta(mockChsDeltaMessage));
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid ChsDelta is given")
    void When_ValidChsDeltaMessage_Expect_ProcessorDoesNotThrow() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage();
        Assertions.assertDoesNotThrow(() -> deltaProcessor.processDelta(mockChsDeltaMessage));
    }

}
