package uk.gov.companieshouse.pscstatement.delta.consumer;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.pscstatement.delta.processor.PscStatementDeltaProcessor;

@ExtendWith(MockitoExtension.class)
class PscStatementDeltaConsumerTest {

    @Mock
    PscStatementDeltaProcessor processor;

    @Test
    void receiveChangedMessageTest() {
        PscStatementDeltaConsumer consumer = new PscStatementDeltaConsumer(processor);
        ChsDelta delta = ChsDelta.newBuilder()
                .setData("test-data")
                .setContextId("test-context-id")
                .setIsDelete(false)
                .build();
        Message<ChsDelta> message = MessageBuilder.createMessage(delta,
                new MessageHeaders(new HashMap<>()));

        consumer.receiveMainMessages(message);

        Mockito.verify(processor, Mockito.times(1)).processDelta(message);
    }

    @Test
    void receiveDeleteMessageTest() {
        PscStatementDeltaConsumer consumer = new PscStatementDeltaConsumer(processor);
        ChsDelta delta = ChsDelta.newBuilder()
                .setData("test-data")
                .setContextId("test-context-id")
                .setIsDelete(true)
                .build();
        Message<ChsDelta> message = MessageBuilder.createMessage(delta,
                new MessageHeaders(new HashMap<>()));

        consumer.receiveMainMessages(message);

        Mockito.verify(processor, Mockito.times(1)).processDeleteDelta(message);
    }
}
