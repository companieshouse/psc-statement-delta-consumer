package uk.gov.companieshouse.pscstatement.delta.utils;

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
    private ChsDelta buildDelta(String data) {
        return ChsDelta.newBuilder()
                .setData(data)
                .setContextId("8Ch2wOs16s5Yqxl2vX42n7GwNHT4")
                .setAttempt(0)
                .build();
    }
}
