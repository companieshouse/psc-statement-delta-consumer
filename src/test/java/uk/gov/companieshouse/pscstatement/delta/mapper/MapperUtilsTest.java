package uk.gov.companieshouse.pscstatement.delta.mapper;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapperUtilsTest {

    private static final String SALT = "salt";
    private static final String DECODED = "test123";
    private static final String ENCODED = "lAEABvLzWoiQCGahiVdDJ0YnyRA";

    private MapperUtils mapperUtils;

    @BeforeEach
    public void setUp() {
        mapperUtils = new MapperUtils(SALT);
    }

    @Test
    void encode_correctly_when_valid_salt_passed() {
        String actualValue = mapperUtils.encode(DECODED);
        assertEquals(actualValue, ENCODED);
    }
}
