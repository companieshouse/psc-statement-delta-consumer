package uk.gov.companieshouse.pscstatement.delta.Exception;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.config.LoggingConfig;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.exception.RetryableErrorInterceptor;
import uk.gov.companieshouse.pscstatement.delta.utils.TestHelper;
import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RetryableErrorInterceptorTest {

    private RetryableErrorInterceptor retryableErrorInterceptor;

    private TestHelper testHelper;

    private Logger logger;

    private LoggingConfig loggingConfig;

    @BeforeEach
    public void setUp() {
        retryableErrorInterceptor = new RetryableErrorInterceptor();
        testHelper = new TestHelper();
        loggingConfig = new LoggingConfig();
        logger = loggingConfig.logger();
        ReflectionTestUtils.setField(retryableErrorInterceptor,"logger",logger);
    }

    @Test
    public void sameRecordReturnedForNonErrorTopic() {
        ProducerRecord<String,Object> record = testHelper.buildRecord("topic", "");
        ProducerRecord<String, Object> actual = retryableErrorInterceptor.onSend(record);
        assertEquals(record,actual);
    }

    @Test
    public void errorTopicReturnedForRetryableError() {
        ProducerRecord<String,Object> record = testHelper.
                buildRecord("-error", RetryableErrorException.class.getName());
        ProducerRecord<String, Object> actual = retryableErrorInterceptor.onSend(record);
        assertEquals(actual, record);
    }

    @Test
    public void invalidTopicReturnedForNonRetryableError() {
        ProducerRecord<String,Object> record = testHelper.
                buildRecord("-error", NonRetryableErrorException.class.getName());
        ProducerRecord<String, Object> actual = retryableErrorInterceptor.onSend(record);
        assertEquals(actual.topic().toString(), "-invalid");
    }
}
