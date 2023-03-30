package uk.gov.companieshouse.pscstatement.delta.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.data.TestData;
import uk.gov.companieshouse.pscstatement.delta.matcher.PscStatementMatcher;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PscStatementsSteps {

    private static WireMockServer wireMockServer;
    @Value("${psc-statement.delta.topic}")
    private String mainTopic;
    @Value("${wiremock.server.port}")
    private String port;
    @Autowired
    public KafkaConsumer<String, Object> kafkaConsumer;

    @Autowired
    private Logger logger;

    private String output;


    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private void configureWiremock() {
        wireMockServer = new WireMockServer(Integer.parseInt(port));
        wireMockServer.start();
        configureFor("localhost", Integer.parseInt(port));
    }


    @Given("the application is running")
    public void theApplicationRunning() {
        assertThat(kafkaTemplate).isNotNull();
    }

    @When("the consumer receives a message")
    public void the_consumer_receives_a_message()  throws Exception {
        configureWiremock();
        stubPutStatement(200, "08694860", "I5tVa-U7URp5pDuXSyEQ8NILVWU");
        this.output = TestData.getStatementOutput();
        ChsDelta delta = new ChsDelta(TestData.getStatementDelta(), 1, "123456789", false);
        kafkaTemplate.send(mainTopic, delta);
        countDown();
    }

    @When("the consumer receives a delete payload")
    public void theConsumerReceivesDelete() throws Exception {
        configureWiremock();
        stubDeleteStatement(200, "09950914", "ENU4UQK4mpX39qvyVkYEGZYt4ME");
        ChsDelta delta = new ChsDelta(TestData.getDeleteData(), 1, "1", true);
        kafkaTemplate.send(mainTopic, delta);
        countDown();
    }

    @Then("a PUT request is sent to the psc statement data api with the encoded data")
    public void put_sent_to_data_api() {
        verify(1, requestMadeFor(new PscStatementMatcher(logger, output,
                "/company/08694860/persons-with-significant-control-statements/I5tVa-U7URp5pDuXSyEQ8NILVWU/internal",
                List.of("statement.etag"))));
    }

    @Then("a DELETE request is sent to the psc statement data api with the encoded Id")
    public void deleteRequestIsSent() {
        verify(1, deleteRequestedFor(urlMatching(
                "/company/09950914/persons-with-significant-control-statements/ENU4UQK4mpX39qvyVkYEGZYt4ME/internal")));
    }

    private void stubPutStatement(int responseCode, String companyNumber, String statementId) {
        stubFor(put(urlEqualTo(String.format(
                "/company/%s/persons-with-significant-control-statements/%s/internal", companyNumber, statementId)))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void stubDeleteStatement(int responseCode, String companyNumber, String statementId) {
        stubFor(delete(urlEqualTo(String.format(
                "/company/%s/persons-with-significant-control-statements/%s/internal", companyNumber, statementId)))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    @After
    public void shutdownWiremock(){
        if (wireMockServer != null)
            wireMockServer.stop();
    }

    private void countDown() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

}
