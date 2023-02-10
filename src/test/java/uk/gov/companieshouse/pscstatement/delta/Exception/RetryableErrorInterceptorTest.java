package uk.gov.companieshouse.pscstatement.delta.Exception;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.exception.RetryableErrorInterceptor;
import uk.gov.companieshouse.pscstatement.delta.utils.TestHelper;
import static org.junit.Assert.assertEquals;


@ExtendWith(MockitoExtension.class)
public class RetryableErrorInterceptorTest {

    private RetryableErrorInterceptor retryableErrorInterceptor;

    private TestHelper testHelper;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setUp() {
        retryableErrorInterceptor = new RetryableErrorInterceptor(logger);
        testHelper = new TestHelper();
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
