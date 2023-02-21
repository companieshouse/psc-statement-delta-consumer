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
        byte[] data = encodedData;

        ChsDelta deserializedObject = deserializer.deserialize("", data);

        Assertions.assertThat(deserializedObject).isInstanceOf(ChsDelta.class);
        ChsDelta delta = testHelper.createChsDelta(false);
        Assertions.assertThat(deserializedObject).isEqualTo(delta);
    }


    @Test
    void When_deserializeFails_throwsNonRetryableError() {
        byte[] data = "Invalid message".getBytes();
        assertThrows(NonRetryableErrorException.class, () -> deserializer.deserialize("", data));
    }

    private byte [] encodedData = {-50,10,123,10,32,32,32,32,34,112,115,99,95,115,116,97,116,101,109,101,110,116,115,34,
            58,32,91,10,32,32,32,32,32,32,32,32,123,10,32,32,32,32,32,32,32,32,32,32,32,32,34,99,111,109,112,97,110,121,
            95,110,117,109,98,101,114,34,58,32,34,48,56,54,57,52,56,54,48,34,44,10,32,32,32,32,32,32,32,32,32,32,32,32,
            34,112,115,99,95,115,116,97,116,101,109,101,110,116,95,105,100,34,58,32,34,51,48,48,48,48,48,48,48,48,50,34,
            44,10,32,32,32,32,32,32,32,32,32,32,32,32,34,115,116,97,116,101,109,101,110,116,34,58,32,34,80,83,67,95,72,
            65,83,95,70,65,73,76,69,68,95,84,79,95,67,79,78,70,73,82,77,95,67,72,65,78,71,69,68,95,68,69,84,65,73,76,83,
            34,44,10,32,32,32,32,32,32,32,32,32,32,32,32,34,115,117,98,109,105,116,116,101,100,95,111,110,34,58,32,34,
            50,48,49,54,48,51,49,52,34,44,10,32,32,32,32,32,32,32,32,32,32,32,32,34,99,101,97,115,101,100,95,111,110,34,
            58,32,34,50,48,50,51,48,49,50,55,34,44,10,32,32,32,32,32,32,32,32,32,32,32,32,34,108,105,110,107,101,100,95,
            112,115,99,34,58,32,123,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,34,110,111,116,105,102,105,99,97,
            116,105,111,110,95,105,100,34,58,32,34,51,48,48,53,48,49,49,57,52,52,34,44,10,32,32,32,32,32,32,32,32,32,32,
            32,32,32,32,32,32,34,112,115,99,95,107,105,110,100,34,58,32,34,105,110,100,105,118,105,100,117,97,108,34,44,
            10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,34,116,105,116,108,101,34,58,32,34,77,114,34,44,10,32,32,
            32,32,32,32,32,32,32,32,32,32,32,32,32,32,34,115,117,114,110,97,109,101,34,58,32,34,67,111,110,103,114,101,
            115,115,112,101,114,115,111,110,108,105,113,117,111,114,34,44,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,
            32,32,34,102,111,114,101,110,97,109,101,34,58,32,34,70,97,120,114,105,118,117,108,101,116,34,10,32,32,32,32,
            32,32,32,32,32,32,32,32,125,44,10,32,32,32,32,32,32,32,32,32,32,32,32,34,114,101,115,116,114,105,99,116,105,
            111,110,115,95,110,111,116,105,99,101,95,114,101,97,115,111,110,34,58,32,34,49,34,10,32,32,32,32,32,32,32,
            32,125,10,32,32,32,32,93,44,10,32,32,32,32,34,67,114,101,97,116,101,100,84,105,109,101,34,58,32,34,49,52,45,
            68,69,67,45,50,50,32,49,49,46,53,49,46,52,52,46,48,48,48,48,48,48,34,44,10,32,32,32,32,34,100,101,108,116,
            97,95,97,116,34,58,32,34,50,48,50,51,48,55,50,51,48,57,51,52,51,53,54,54,49,53,57,51,34,10,125,0,56,77,108,
            104,104,105,76,77,105,82,90,108,109,50,115,119,75,89,104,51,73,88,76,57,69,117,113,120,48,0 };
}
