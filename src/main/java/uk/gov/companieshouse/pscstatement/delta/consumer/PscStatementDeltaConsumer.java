package uk.gov.companieshouse.pscstatement.delta.consumer;

import consumer.exception.NonRetryableErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.pscstatement.delta.processor.PscStatementDeltaProcessor;

@Component
public class PscStatementDeltaConsumer {
    private final PscStatementDeltaProcessor deltaProcessor;
    @Autowired
    public PscStatementDeltaConsumer(PscStatementDeltaProcessor deltaProcessor) {
        this.deltaProcessor = deltaProcessor;
    }

    /**
     * Receives Main topic messages.
     */
    @RetryableTopic(attempts = "${psc-statement.delta.retry-attempts}",
            backoff = @Backoff(delayExpression = "${psc-statement.delta.backoff-delay}"),
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            retryTopicSuffix = "-retry",
            dltTopicSuffix = "-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(topics = "${psc-statement.delta.topic}",
            groupId = "${psc-statement.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> message) {
        ChsDelta chsDelta = message.getPayload();
        if (chsDelta.getIsDelete()) {
            deltaProcessor.processDeleteDelta(message);
        } else {
            deltaProcessor.processDelta(message);
        }
    }
}
