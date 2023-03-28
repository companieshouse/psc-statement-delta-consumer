package uk.gov.companieshouse.pscstatement.delta.data;

import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestData {
    public static String getStatementDelta() {
        String path = "src/itest/resources/json/input/psc_statement_delta.json";
        return readFile(path);
    }

    public static String getStatementOutput() {
        String path = "src/itest/resources/json/output/psc_statement_output.json";
        return readFile(path);
    }

    private static String readFile(String path) {
        String data;
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(new File(path))));
        } catch (IOException e) {
            data = null;
        }
        return data;
    }
}
