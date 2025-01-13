package uk.gov.companieshouse.pscstatement.delta.transformer;

import static uk.gov.companieshouse.pscstatement.delta.PscStatementDeltaConsumerApplication.APPLICATION_NAME_SPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import consumer.exception.NonRetryableErrorException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatementDeleteDelta;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatement.delta.logging.DataMapHolder;

@Component
public class PscStatementDeltaDeserialiser {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private static final String UPSERT_ERROR_MESSAGE = "Unable to deserialise UPSERT delta: [%s]";
    private static final String DELETE_ERROR_MESSAGE = "Unable to deserialise DELETE delta: [%s]";

    private final ObjectMapper objectMapper;

    public PscStatementDeltaDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PscStatementDelta deserialisePscStatementDelta(String data) {
        try {
            return objectMapper.readValue(data, PscStatementDelta.class);
        } catch (JsonProcessingException ex) {
            LOGGER.error(UPSERT_ERROR_MESSAGE.formatted(data), ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(UPSERT_ERROR_MESSAGE.formatted(data), ex);
        }
    }

    public PscStatementDeleteDelta deserialisePscStatementDeleteDelta(String data) {
        try {
            return objectMapper.readValue(data, PscStatementDeleteDelta.class);
        } catch (JsonProcessingException ex) {
            LOGGER.error(DELETE_ERROR_MESSAGE.formatted(data), ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(DELETE_ERROR_MESSAGE.formatted(data), ex);
        }
    }
}
