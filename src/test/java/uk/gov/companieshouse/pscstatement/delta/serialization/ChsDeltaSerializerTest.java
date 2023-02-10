package uk.gov.companieshouse.pscstatement.delta.serialization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.utils.TestHelper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ChsDeltaSerializerTest {

    private String payload = "{\"data\": \"{\\n  \\\"company_number\\\": \\\"09950914\\\",\\n  \\\"psc_statement_id\\\":" +
            " \\\"181ZGWm42-hTgP-LBAQTjWQnVzM\\\",\\n  \\\"action\\\": \\\"DELETE\\\"\\n}\", \"attempt\": 0, \"context_id\":" +
            " \"MlhhiLMiRZlm2swKYh3IXL9Euqx0\", \"is_delete\": true}\n";

    private ChsDeltaSerializer chsDeltaSerializer;

    private TestHelper testHelper;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setUp() {
        testHelper = new TestHelper();
        chsDeltaSerializer = new ChsDeltaSerializer(logger);
    }

    @Test
    public void serializeChsDelta() throws IOException {
        ChsDelta delta = testHelper.createChsDelta(true);
        byte[] actual = chsDeltaSerializer.serialize("test", delta);
        Assertions.assertThat(actual).isEqualTo(data);
    }

    @Test
    public void serializeChsDeltaString() {
        byte[] actual = chsDeltaSerializer.serialize("test", payload);
        Assertions.assertThat(actual).isEqualTo(stringData);
    }

    @Test
    public void serializeNull() {
        byte[] actual = chsDeltaSerializer.serialize("test", null);
        Assertions.assertThat(actual).isEqualTo(null);
    }

    @Test
    public void serializeBytes() {
        byte[] actual = chsDeltaSerializer.serialize("test", data);
        Assertions.assertThat(actual).isEqualTo(data);
    }

    @Test
    public void ThrowExceptionWhenEmptyObjectProvided() {
        ChsDelta chsDelta = new ChsDelta();
    assertThrows(Exception.class, () -> {
        chsDeltaSerializer.serialize("test", chsDelta);
        });
    }

    private final byte[] data = {-38, 1, 123, 10, 32, 32, 34, 99, 111, 109, 112, 97, 110, 121, 95, 110, 117, 109, 98,
            101, 114, 34, 58, 32, 34, 48, 57, 57, 53, 48, 57, 49, 52, 34, 44, 10, 32, 32, 34, 112, 115, 99, 95, 115,
            116, 97, 116, 101, 109, 101, 110, 116, 95, 105, 100, 34, 58, 32, 34, 49, 56, 49, 90, 71, 87, 109, 52, 50,
            45, 104, 84, 103, 80, 45, 76, 66, 65, 81, 84, 106, 87, 81, 110, 86, 122, 77, 34, 44, 10, 32, 32, 34, 97, 99,
            116, 105, 111, 110, 34, 58, 32, 34, 68, 69, 76, 69, 84, 69, 34, 10, 125, 0, 56, 77, 108, 104, 104, 105, 76,
            77, 105, 82, 90, 108, 109, 50, 115, 119, 75, 89, 104, 51, 73, 88, 76, 57, 69, 117, 113, 120, 48, 1};

    private final byte[] stringData = {123, 34, 100, 97, 116, 97, 34, 58, 32, 34, 123, 92, 110, 32, 32, 92, 34, 99, 111,
            109, 112, 97, 110, 121, 95, 110, 117, 109, 98, 101, 114, 92, 34, 58, 32, 92, 34, 48, 57, 57, 53, 48, 57, 49,
            52, 92, 34, 44, 92, 110, 32, 32, 92, 34, 112, 115, 99, 95, 115, 116, 97, 116, 101, 109, 101, 110, 116, 95,
            105, 100, 92, 34, 58, 32, 92, 34, 49, 56, 49, 90, 71, 87, 109, 52, 50, 45, 104, 84, 103, 80, 45, 76, 66, 65,
            81, 84, 106, 87, 81, 110, 86, 122, 77, 92, 34, 44, 92, 110, 32, 32, 92, 34, 97, 99, 116, 105, 111, 110, 92,
            34, 58, 32, 92, 34, 68, 69, 76, 69, 84, 69, 92, 34, 92, 110, 125, 34, 44, 32, 34, 97, 116, 116, 101, 109,
            112, 116, 34, 58, 32, 48, 44, 32, 34, 99, 111, 110, 116, 101, 120, 116, 95, 105, 100, 34, 58, 32, 34, 77,
            108, 104, 104, 105, 76, 77, 105, 82, 90, 108, 109, 50, 115, 119, 75, 89, 104, 51, 73, 88, 76, 57, 69, 117,
            113, 120, 48, 34, 44, 32, 34, 105, 115, 95, 100, 101, 108, 101, 116, 101, 34, 58, 32, 116, 114, 117, 101,
            125, 10};
}
