package uk.gov.companieshouse.pscstatement.delta.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import uk.gov.companieshouse.delta.ChsDelta;

import java.util.HashMap;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StructuredLoggingKafkaListenerAspectTest {

    @Mock
    ProceedingJoinPoint proceedingJoinPoint;

    StructuredLoggingKafkaListenerAspect aspect = new StructuredLoggingKafkaListenerAspect();

    @Test
    void manageStructuredLoggingTest(){
        ChsDelta delta = ChsDelta.newBuilder()
                .setData("test-data")
                .setContextId("test-context-id")
                .setIsDelete(true)
                .build();
        Message<ChsDelta> message = MessageBuilder.createMessage(delta, new MessageHeaders(new HashMap<>()));
        Message<?>[] messages = new Message[]{message};
        when(proceedingJoinPoint.getArgs()).thenReturn(messages);

        Assertions.assertDoesNotThrow(() -> aspect.manageStructuredLogging(proceedingJoinPoint));
    }

    @Test
    void manageStructuredLoggingErrorTest() throws Throwable{
        Message<String> message = MessageBuilder.createMessage("message", new MessageHeaders(new HashMap<>()));
        Message<?>[] messages = new Message[]{message};
        when(proceedingJoinPoint.getArgs()).thenReturn(messages);
        when(proceedingJoinPoint.proceed()).thenThrow(new Exception());

        Assertions.assertThrows(Exception.class, () -> aspect.manageStructuredLogging(proceedingJoinPoint));
    }
}
