package uk.gov.companieshouse.pscstatement.delta.utils;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.delta.ChsDelta;

import java.io.IOException;
import java.io.InputStreamReader;

public class TestHelper {
    public ChsDelta createChsDelta(boolean isDelete) throws IOException {
        String resource;
        if (isDelete) {
            resource = "psc-statement-delete-delta-example.json";
        } else {
            resource = "psc-statement-delta-example.json";
        }
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildDelta(data, isDelete);
    }

    public Message<ChsDelta> createChsDeltaMessage(boolean isDelete) throws IOException {
        String resource;
        if (isDelete) {
            resource = "psc-statement-delete-delta-example.json";
        } else {
            resource = "psc-statement-delta-example.json";
        }
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildMessage(data, isDelete);
    }

    public Message<ChsDelta> createInvalidChsDeltaMessage() {
        return buildMessage("This is some invalid data", false);
    }

    private ChsDelta buildDelta(String data, boolean isDelete) {
        return ChsDelta.newBuilder()
                .setData(data)
                .setContextId("ogIuTpT6GZlkkAZsXtjuPGtOrYdk")
                .setAttempt(0)
                .setIsDelete(isDelete)
                .build();
    }
    private Message<ChsDelta> buildMessage (String data, boolean isDelete) {
        return MessageBuilder
                .withPayload(buildDelta(data, isDelete))
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "test")
                .setHeader("PSC_STATEMENT_DELTA_RETRY_COUNT", 1)
                .build();
    }
}
