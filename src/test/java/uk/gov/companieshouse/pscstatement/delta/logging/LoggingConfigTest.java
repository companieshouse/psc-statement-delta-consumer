package uk.gov.companieshouse.pscstatement.delta.logging;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.config.LoggingConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoggingConfigTest {

    @Test
    void testLoggingConfig(){
        LoggingConfig loggingConfig = new LoggingConfig();
        Logger logger = loggingConfig.logger();

        assertEquals(logger, LoggingConfig.getLogger());
    }
}
