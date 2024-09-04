package uk.gov.companieshouse.pscstatement.delta.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.util.FileCopyUtils;

public class TestData {

    public static String getStatementDelta(String type) {
        String path = "src/itest/resources/json/input/psc_statement_delta_" + type + ".json";
        return readFile(path);
    }

    public static String getStatementOutput(String type) {
        String path = "src/itest/resources/json/output/psc_statement_output_" + type + ".json";
        return readFile(path);
    }

    public static String getDeleteData() {
        String path = "src/itest/resources/json/input/psc_statement_delete.json";
        return readFile(path);
    }

    private static String readFile(String path) {
        String data;
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(path)));
        } catch (IOException e) {
            data = null;
        }
        return data;
    }
}
