package uk.gov.companieshouse.pscstatement.delta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Configuration class for logging.
 */
@Configuration
public class LoggingConfig {

    private static Logger staticLogger;

    /**
     * Creates a logger with specified namespace.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        Logger loggerBean = LoggerFactory.getLogger("psc-delta-consumer");
        staticLogger = loggerBean;
        return loggerBean;
    }

    public static Logger getLogger() {
        return staticLogger;
    }
}
