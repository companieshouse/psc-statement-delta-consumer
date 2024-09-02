package uk.gov.companieshouse.pscstatement.delta.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;


public class MapperUtilsTest {

    private final String decoded = "test123";
    private final String encoded = "J723Fh25xbc3NMoBCYLJhqNBhY4";
    private MapperUtils mapperUtils;

    @BeforeEach
    public void setUp() {
        mapperUtils = new MapperUtils();
    }

    @Test
    public void encode_correctly_when_valid_salt_passed() {
        ReflectionTestUtils.setField(mapperUtils, "salt", decoded);
        String actualValue = mapperUtils.encode(decoded);
        assertEquals(actualValue, encoded);
    }

    @Test
    public void fail_to_encode_correctly_with_no_salt() {
        String actualValue = mapperUtils.encode(decoded);
        assertNotEquals(actualValue, encoded);
    }
}
