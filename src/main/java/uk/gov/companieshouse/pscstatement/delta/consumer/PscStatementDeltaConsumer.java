package uk.gov.companieshouse.pscstatement.delta.consumer;

import static java.lang.String.format;

import java.io.IOException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.pscstatement.delta.processor.PscStatementDeltaProcessor;


@Component
public class PscStatementDeltaConsumer {
    private final Logger logger;
    private final PscStatementDeltaProcessor deltaProcessor;

    @Autowired
    public PscStatementDeltaConsumer(PscStatementDeltaProcessor deltaProcessor,
            Logger logger) {
        this.deltaProcessor = deltaProcessor;
        this.logger = logger;
    }

    /**
     * Receives Main topic messages.
     */
    @RetryableTopic(attempts = "${psc-statement.delta.retry-attempts}",
            backoff = @Backoff(delayExpression = "${psc-statement.delta.backoff-delay}"),
            fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC,
            retryTopicSuffix = "-retry",
            dltTopicSuffix = "-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(topics = "${psc-statement.delta.topic}",
            groupId = "${psc-statement.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> message,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION_ID) String partition,
                                    @Header(KafkaHeaders.OFFSET) String offset) throws IOException,
            URIValidationException {
        Instant startTime = Instant.now();
        ChsDelta chsDelta = message.getPayload();
        try {
            if (chsDelta.getIsDelete()) {
                deltaProcessor.processDeleteDelta(message);
            } else {
                deltaProcessor.processDelta(message);
            }
        } catch (Exception exception) {
            logger.error(format("Exception occurred while processing "
                    + "message on the topic: %s", topic), exception, null);
            throw exception;
        }
    }
}
