package uk.gov.companieshouse.pscstatement.delta.serialization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ChsDeltaSerializerTest {

    private String payload = "{\"data\": \"{\\n  \\\"company_number\\\": \\\"09950914\\\"," +
            "\\n  \\\"psc_statement_id\\\": \\\"181ZGWm42-hTgP-LBAQTjWQnVzM\\\"," +
            "\\n  \\\"action\\\": \\\"DELETE\\\"\\n}\", \"attempt\": 0," +
            " \"context_id\": \"ygLFhuMVygRctDXncwnaaytFft2F\", \"is_delete\": true}";

    private ChsDeltaSerializer chsDeltaSerializer;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setUp() {
        chsDeltaSerializer = new ChsDeltaSerializer(logger);
    }

    @Test
    public void serializeChsDelta() {
        byte[] actual = chsDeltaSerializer.serialize("test", payload);
        Assertions.assertThat(actual).isEqualTo(data);
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

    private final byte[] data = {123, 34, 100, 97, 116, 97, 34, 58, 32, 34, 123, 92, 110, 32, 32, 92, 34, 99, 111, 109,
                112, 97, 110, 121, 95, 110, 117, 109, 98, 101, 114, 92, 34, 58, 32, 92, 34, 48, 57, 57, 53, 48,
                57, 49, 52, 92, 34, 44, 92, 110, 32, 32, 92, 34, 112, 115, 99, 95, 115, 116, 97, 116, 101, 109,
                101, 110, 116, 95, 105, 100, 92, 34, 58, 32, 92, 34, 49, 56, 49, 90, 71, 87, 109, 52, 50, 45, 104,
                84, 103, 80, 45, 76, 66, 65, 81, 84, 106, 87, 81, 110, 86, 122, 77, 92, 34, 44, 92, 110, 32, 32, 92,
                34, 97, 99, 116, 105, 111, 110, 92, 34, 58, 32, 92, 34, 68, 69, 76, 69, 84, 69, 92, 34, 92, 110, 125,
                34, 44, 32, 34, 97, 116, 116, 101, 109, 112, 116, 34, 58, 32, 48, 44, 32, 34, 99, 111, 110, 116, 101,
                120, 116, 95, 105, 100, 34, 58, 32, 34, 121, 103, 76, 70, 104, 117, 77, 86, 121, 103, 82, 99, 116, 68,
                88, 110, 99, 119, 110, 97, 97, 121, 116, 70, 102, 116, 50, 70, 34, 44, 32, 34, 105, 115, 95, 100, 101,
                108, 101, 116, 101, 34, 58, 32, 116, 114, 117, 101, 125};
}
