package uk.gov.companieshouse.pscstatement.delta.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS;
import static org.springframework.kafka.support.KafkaHeaders.OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

import consumer.exception.NonRetryableErrorException;
import consumer.exception.RetryableErrorException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.regex.Pattern;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import uk.gov.companieshouse.delta.ChsDelta;

@ExtendWith(MockitoExtension.class)
class StructuredLoggingKafkaListenerAspectTest {

    private static final String CONTEXT_ID = "context_id";
    private static final String TOPIC = "psc-statement-delta-consumer";
    private static final Pattern INFO_EVENT_PATTERN = Pattern.compile(
            "event: info|\"event\":\"info\"");
    private static final Pattern ERROR_EVENT_PATTERN = Pattern.compile(
            "event: error|\"event\":\"error\"");
    private static final Pattern MAX_RETRY_ATTEMPTS_REACHED_PATTERN = Pattern.compile(
            "error: Max retry attempts reached|\"message\":\"Max retry attempts reached\"");
    private static final Pattern INVALID_PAYLOAD_PATTERN = Pattern.compile(
            "error: Invalid payload type, payload: \\[message payload]|\"message\":\"Invalid payload type, payload: \\[message payload]\"");
    private static final Pattern REQUEST_ID_INITIALISED_PATTERN = Pattern.compile(
            "request_id: context_id|\"request_id\":\"context_id\"");
    private static final Pattern REQUEST_ID_UNINITIALISED_PATTERN = Pattern.compile(
            "request_id: uninitialised|\"event\":\"error\"");
    private static final Pattern RETRY_COUNT_ZERO_PATTERN = Pattern.compile(
            "retry_count: 0|\"retry_count\":0");
    private static final Pattern RETRY_COUNT_FOUR_PATTERN = Pattern.compile(
            "retry_count: 4|\"retry_count\":4");
    private static final Pattern MAIN_TOPIC_PATTERN = Pattern.compile(
            "topic: psc-statement-delta-consumer|\"topic\":\"psc-statement-delta-consumer\"");
    private static final Pattern PARTITION_ZERO_PATTERN = Pattern.compile(
            "partition: 0|\"partition\":0");
    private static final Pattern OFFSET_ZERO_PATTERN = Pattern.compile(
            "offset: 0|\"offset\":0");

    private final StructuredLoggingKafkaListenerAspect aspect = new StructuredLoggingKafkaListenerAspect(4);

    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private Message<ChsDelta> message;
    @Mock
    private ChsDelta delta;
    @Mock
    private Message<String> invalidMessage;

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldManageStructuredLogging(CapturedOutput capture) throws Throwable {
        // given
        MessageHeaders headers = new MessageHeaders(
                Map.of(
                        RECEIVED_TOPIC, TOPIC,
                        RECEIVED_PARTITION, 0,
                        OFFSET, 0L));
        Object expected = "result";
        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(message.getPayload()).thenReturn(delta);
        when(message.getHeaders()).thenReturn(headers);
        when(delta.getContextId()).thenReturn(CONTEXT_ID);
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object actual = aspect.manageStructuredLogging(joinPoint);

        //then
        assertEquals(expected, actual);
        assertTrue(capture.getOut().contains("Processed delta"));
        verifyInfoLogMap(capture);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldManageStructuredLoggingDeleteDelta(CapturedOutput capture) throws Throwable {
        // given
        MessageHeaders headers = new MessageHeaders(
                Map.of(
                        RECEIVED_TOPIC, TOPIC,
                        RECEIVED_PARTITION, 0,
                        OFFSET, 0L));
        Object expected = "result";
        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(message.getPayload()).thenReturn(delta);
        when(message.getHeaders()).thenReturn(headers);
        when(delta.getContextId()).thenReturn(CONTEXT_ID);
        when(delta.getIsDelete()).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object actual = aspect.manageStructuredLogging(joinPoint);

        //then
        assertEquals(expected, actual);
        assertTrue(capture.getOut().contains("Processed DELETE delta"));
        verifyInfoLogMap(capture);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldLogInfoWhenRetryableException(CapturedOutput capture) throws Throwable {
        // given
        MessageHeaders headers = new MessageHeaders(
                Map.of(
                        RECEIVED_TOPIC, TOPIC,
                        RECEIVED_PARTITION, 0,
                        OFFSET, 0L));
        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(message.getPayload()).thenReturn(delta);
        when(message.getHeaders()).thenReturn(headers);
        when(delta.getContextId()).thenReturn(CONTEXT_ID);
        when(joinPoint.proceed()).thenThrow(RetryableErrorException.class);

        // when
        Executable actual = () -> aspect.manageStructuredLogging(joinPoint);

        //then
        assertThrows(RetryableErrorException.class, actual);
        assertTrue(capture.getOut().contains("RetryableErrorException exception thrown"));
        verifyInfoLogMap(capture);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldLogInfoWhenRetryableExceptionMaxAttempts(CapturedOutput capture) throws Throwable {
        // given
        MessageHeaders headers = new MessageHeaders(
                Map.of(
                        // attempts header returns a byte array not an integer
                        DEFAULT_HEADER_ATTEMPTS, ByteBuffer.allocate(4).putInt(5).array(),
                        RECEIVED_TOPIC, TOPIC,
                        RECEIVED_PARTITION, 0,
                        OFFSET, 0L));
        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(message.getPayload()).thenReturn(delta);
        when(message.getHeaders()).thenReturn(headers);
        when(delta.getContextId()).thenReturn(CONTEXT_ID);
        when(joinPoint.proceed()).thenThrow(RetryableErrorException.class);

        // when
        Executable actual = () -> aspect.manageStructuredLogging(joinPoint);

        //then
        assertThrows(RetryableErrorException.class, actual);
        assertTrue(ERROR_EVENT_PATTERN.matcher(capture.getOut()).find());
        assertTrue(MAX_RETRY_ATTEMPTS_REACHED_PATTERN.matcher(capture.getOut()).find());
        assertTrue(REQUEST_ID_INITIALISED_PATTERN.matcher(capture.getOut()).find());
        assertTrue(RETRY_COUNT_FOUR_PATTERN.matcher(capture.getOut()).find());
        assertTrue(MAIN_TOPIC_PATTERN.matcher(capture.getOut()).find());
        assertTrue(PARTITION_ZERO_PATTERN.matcher(capture.getOut()).find());
        assertTrue(OFFSET_ZERO_PATTERN.matcher(capture.getOut()).find());
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldLogInfoWhenInvalidPayload(CapturedOutput capture) {
        // given
        MessageHeaders headers = new MessageHeaders(
                Map.of(
                        RECEIVED_TOPIC, TOPIC,
                        RECEIVED_PARTITION, 0,
                        OFFSET, 0L));
        when(joinPoint.getArgs()).thenReturn(new Object[]{invalidMessage});
        when(invalidMessage.getPayload()).thenReturn("message payload");
        when(invalidMessage.getHeaders()).thenReturn(headers);

        // when
        Executable actual = () -> aspect.manageStructuredLogging(joinPoint);

        //then
        assertThrows(NonRetryableErrorException.class, actual);
        assertTrue(ERROR_EVENT_PATTERN.matcher(capture.getOut()).find());
        assertTrue(INVALID_PAYLOAD_PATTERN.matcher(capture.getOut()).find());
        assertTrue(REQUEST_ID_UNINITIALISED_PATTERN.matcher(capture.getOut()).find());
        assertFalse(capture.getOut().contains("retry_count"));
        assertFalse(capture.getOut().contains("topic"));
        assertFalse(capture.getOut().contains("partition"));
        assertFalse(capture.getOut().contains("offset"));
    }

    private static void verifyInfoLogMap(CapturedOutput capture) {
        assertTrue(INFO_EVENT_PATTERN.matcher(capture.getOut()).find());
        assertTrue(REQUEST_ID_INITIALISED_PATTERN.matcher(capture.getOut()).find());
        assertTrue(RETRY_COUNT_ZERO_PATTERN.matcher(capture.getOut()).find());
        assertTrue(MAIN_TOPIC_PATTERN.matcher(capture.getOut()).find());
        assertTrue(PARTITION_ZERO_PATTERN.matcher(capture.getOut()).find());
        assertTrue(OFFSET_ZERO_PATTERN.matcher(capture.getOut()).find());
    }
}
