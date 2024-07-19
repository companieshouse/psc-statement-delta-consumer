package uk.gov.companieshouse.pscstatement.delta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatement.delta.PscStatementDeltaConsumerApplication;

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
        var loggerBean = LoggerFactory.getLogger(PscStatementDeltaConsumerApplication.NAMESPACE);
        staticLogger = loggerBean;
        return loggerBean;
    }

    public static Logger getLogger() {
        return staticLogger;
    }
}
