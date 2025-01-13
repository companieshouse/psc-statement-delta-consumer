package uk.gov.companieshouse.pscstatement.delta.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import consumer.exception.NonRetryableErrorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.PscStatementDeleteDelta;
import uk.gov.companieshouse.api.delta.PscStatementDelta;

@ExtendWith(MockitoExtension.class)
class PscStatementDeltaDeserialiserTest {
    public static final String PSC_STATEMENT_DELTA = "psc statement delta json string";
    public static final String PSC_STATEMENT_DELETE_DELTA = "psc statement delete delta json string";
    @InjectMocks
    private PscStatementDeltaDeserialiser deserialiser;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private PscStatementDelta expectedDelta;
    @Mock
    private PscStatementDeleteDelta expectedDeleteDelta;

    @Test
    void shouldDeserialiseFilingHistoryDelta() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(PscStatementDelta.class))).thenReturn(expectedDelta);

        // when
        PscStatementDelta actual = deserialiser.deserialisePscStatementDelta(PSC_STATEMENT_DELTA);

        // then
        assertEquals(expectedDelta, actual);
        verify(objectMapper).readValue(PSC_STATEMENT_DELTA, PscStatementDelta.class);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenJsonProcessingExceptionThrown() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(PscStatementDelta.class))).thenThrow(
                JsonProcessingException.class);

        // when
        Executable executable = () -> deserialiser.deserialisePscStatementDelta(PSC_STATEMENT_DELTA);

        // then
        NonRetryableErrorException actual = assertThrows(NonRetryableErrorException.class, executable);
        assertEquals("Unable to deserialise UPSERT delta: [psc statement delta json string]",
                actual.getMessage());
        verify(objectMapper).readValue(PSC_STATEMENT_DELTA, PscStatementDelta.class);
    }

    @Test
    void shouldDeserialiseFilingHistoryDeleteDelta() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(PscStatementDeleteDelta.class))).thenReturn(expectedDeleteDelta);

        // when
        PscStatementDeleteDelta actual = deserialiser.deserialisePscStatementDeleteDelta(PSC_STATEMENT_DELETE_DELTA);

        // then
        assertEquals(expectedDeleteDelta, actual);
        verify(objectMapper).readValue(PSC_STATEMENT_DELETE_DELTA, PscStatementDeleteDelta.class);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenJsonProcessingExceptionThrownFromDeleteDelta()
            throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(PscStatementDeleteDelta.class))).thenThrow(
                JsonProcessingException.class);

        // when
        Executable executable = () -> deserialiser.deserialisePscStatementDeleteDelta(PSC_STATEMENT_DELETE_DELTA);

        // then
        NonRetryableErrorException actual = assertThrows(NonRetryableErrorException.class, executable);
        assertEquals("Unable to deserialise DELETE delta: [psc statement delete delta json string]",
                actual.getMessage());
        verify(objectMapper).readValue(PSC_STATEMENT_DELETE_DELTA, PscStatementDeleteDelta.class);
    }
}
