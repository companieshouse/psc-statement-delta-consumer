package uk.gov.companieshouse.pscstatement.delta.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscstatement.delta.data.TestData;
import consumer.matcher.RequestMatcher;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
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

    @When("the consumer receives a message of {string}")
    public void the_consumer_receives_a_message(String type)  throws Exception {
        configureWiremock();
        stubPutStatement(200);
        this.output = TestData.getStatementOutput(type);
        String input = TestData.getStatementDelta(type);
        ChsDelta delta = new ChsDelta(input, 1, "123456789", false);
        kafkaTemplate.send(mainTopic, delta);
        countDown();
    }

    @When("the consumer receives a delete payload")
    public void theConsumerReceivesDelete() throws Exception {
        configureWiremock();
        stubDeleteStatement(200);
        ChsDelta delta = new ChsDelta(TestData.getDeleteData(), 1, "1", true);
        kafkaTemplate.send(mainTopic, delta);
        countDown();
    }

    @When("the consumer receives an invalid delete payload")
    public void theConsumerReceivesInvalidDelete() throws Exception {
        configureWiremock();
        ChsDelta delta = new ChsDelta("invalid", 1, "1", true);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("^the consumer receives a delete message but the data api returns a (\\d*)$")
    public void theConsumerReceivesDeleteMessageButDataApiReturns(int responseCode) throws Exception{
        configureWiremock();
        stubDeleteStatement(responseCode);
        ChsDelta delta = new ChsDelta(TestData.getDeleteData(), 1, "1", true);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("^the consumer receives a (.*) message but the data api returns a (\\d*)$")
    public void theConsumerReceivesMessageButDataApiReturns(String type, int responseCode) throws Exception{
        configureWiremock();
        stubPutStatement(responseCode);
        ChsDelta delta = new ChsDelta(TestData.getStatementDelta(type), 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("an invalid avro message is sent")
    public void invalidAvroMessageIsSent() throws Exception {
        kafkaTemplate.send(mainTopic, "InvalidData");

        countDown();
    }

    @When("a message with invalid data is sent")
    public void messageWithInvalidDataIsSent() throws Exception {
        ChsDelta delta = new ChsDelta("InvalidData", 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @Then("a PUT request is sent to the psc statement data api with the encoded data")
    public void put_sent_to_data_api() {
        verify(1, requestMadeFor(new RequestMatcher(logger, output,
                "/company/08694860/persons-with-significant-control-statements/I5tVa-U7URp5pDuXSyEQ8NILVWU/internal",
                List.of("statement.etag"))));
    }

    @Then("a DELETE request is sent to the psc statement data api with the encoded Id")
    public void deleteRequestIsSent() {
        verify(1, deleteRequestedFor(urlMatching(
                "/company/09950914/persons-with-significant-control-statements/ENU4UQK4mpX39qvyVkYEGZYt4ME/internal")));
    }

    @Then("^the message should be moved to topic (.*)$")
    public void theMessageShouldBeMovedToTopic(String topic) {
        ConsumerRecord<String, Object> singleRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, topic);

        assertThat(singleRecord.value()).isNotNull();
    }

    @Then("^the message should retry (\\d*) times and then error$")
    public void theMessageShouldRetryAndError(int retries) {
        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(kafkaConsumer);
        Iterable<ConsumerRecord<String, Object>> retryRecords =  records.records("psc-statement-delta-retry");
        Iterable<ConsumerRecord<String, Object>> errorRecords =  records.records("psc-statement-delta-error");

        int actualRetries = (int) StreamSupport.stream(retryRecords.spliterator(), false).count();
        int errors = (int) StreamSupport.stream(errorRecords.spliterator(), false).count();

        assertThat(actualRetries).isEqualTo(retries);
        assertThat(errors).isEqualTo(1);
    }

    private void stubPutStatement(int responseCode) {
        stubFor(put(urlEqualTo(
                "/company/08694860/persons-with-significant-control-statements/I5tVa-U7URp5pDuXSyEQ8NILVWU/internal"))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void stubDeleteStatement(int responseCode) {
        stubFor(delete(urlEqualTo(
                "/company/09950914/persons-with-significant-control-statements/ENU4UQK4mpX39qvyVkYEGZYt4ME/internal"))
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
