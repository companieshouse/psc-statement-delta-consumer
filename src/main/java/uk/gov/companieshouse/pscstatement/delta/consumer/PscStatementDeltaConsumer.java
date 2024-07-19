package uk.gov.companieshouse.pscstatement.delta.consumer;

import consumer.exception.NonRetryableErrorException;

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
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatement.delta.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatement.delta.processor.PscStatementDeltaProcessor;

import static uk.gov.companieshouse.pscstatement.delta.PscStatementDeltaConsumerApplication.NAMESPACE;


@Component
public class PscStatementDeltaConsumer {
    private final PscStatementDeltaProcessor deltaProcessor;
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Autowired
    public PscStatementDeltaConsumer(PscStatementDeltaProcessor deltaProcessor) {
        this.deltaProcessor = deltaProcessor;
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
                                    @Header(KafkaHeaders.OFFSET) String offset) {
        ChsDelta chsDelta = message.getPayload();
        String contextId = chsDelta.getContextId();
        if (chsDelta.getIsDelete()) {
            LOGGER.infoContext(contextId,"Delete message received",
                    DataMapHolder.getLogMap());
            deltaProcessor.processDeleteDelta(message);
        } else {
            LOGGER.infoContext(contextId,"Resource changed message received",
                    DataMapHolder.getLogMap());
            deltaProcessor.processDelta(message);
        }
    }
}
