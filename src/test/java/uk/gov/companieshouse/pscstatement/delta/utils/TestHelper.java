package uk.gov.companieshouse.pscstatement.delta.utils;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.delta.ChsDelta;

import java.io.IOException;
import java.io.InputStreamReader;

public class TestHelper {
    public ChsDelta createChsDelta() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("psc-statement-delta-example.json"));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildDelta(data);
    }

    public Message<ChsDelta> createChsDeltaMessage() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("psc-statement-delta-example.json"));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildMessage(data);
    }

    public Message<ChsDelta> createInvalidChsDeltaMessage() {
        return buildMessage("This is some invalid data");
    }

    private ChsDelta buildDelta(String data) {
        return ChsDelta.newBuilder()
                .setData(data)
                .setContextId("MlhhiLMiRZlm2swKYh3IXL9Euqx0")
                .setAttempt(0)
                .build();
    }
    private Message<ChsDelta> buildMessage (String data) {
        return MessageBuilder
                .withPayload(buildDelta(data))
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "test")
                .setHeader("PSC_STATEMENT_DELTA_RETRY_COUNT", 1)
                .build();
    }
}
