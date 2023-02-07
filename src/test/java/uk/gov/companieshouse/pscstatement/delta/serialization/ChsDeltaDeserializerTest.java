package uk.gov.companieshouse.pscstatement.delta.serialization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.utils.TestHelper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class ChsDeltaDeserializerTest {

    @Mock
    private Logger logger;
    private ChsDeltaDeserializer deserializer;
    private TestHelper testHelper = new TestHelper();
    @BeforeEach
    public void init() {
        deserializer = new ChsDeltaDeserializer(logger);
    }

    @Test
    void When_deserialize_Expect_ValidChsDeltaObject() throws IOException {
        byte[] data = testHelper.createByteArray();

        ChsDelta deserializedObject = deserializer.deserialize("", data);

        Assertions.assertThat(deserializedObject).isInstanceOf(ChsDelta.class);
        ChsDelta delta = testHelper.createChsDelta();
        Assertions.assertThat(deserializedObject).isEqualTo(delta);
    }


    @Test
    void When_deserializeFails_throwsNonRetryableError() {
        byte[] data = "Invalid message".getBytes();
        assertThrows(NonRetryableErrorException.class, () -> deserializer.deserialize("", data));
    }
}
