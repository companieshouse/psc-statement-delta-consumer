package uk.gov.companieshouse.pscstatement.delta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PscStatementDeltaConsumerApplication {

    public static final String NAMESPACE = "psc-statement-delta-consumer";

    public static void main(String[] args) {
        SpringApplication.run(PscStatementDeltaConsumerApplication.class, args);
    }

}
