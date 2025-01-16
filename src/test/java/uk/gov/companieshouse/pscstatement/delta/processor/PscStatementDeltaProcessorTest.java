package uk.gov.companieshouse.pscstatement.delta.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import consumer.exception.RetryableErrorException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.pscstatement.delta.mapper.MapperUtils;
import uk.gov.companieshouse.pscstatement.delta.service.ApiClientService;
import uk.gov.companieshouse.pscstatement.delta.transformer.PscStatementApiTransformer;
import uk.gov.companieshouse.pscstatement.delta.transformer.PscStatementDeltaDeserialiser;
import uk.gov.companieshouse.pscstatement.delta.utils.TestHelper;


@ExtendWith(MockitoExtension.class)
class PscStatementDeltaProcessorTest {

    private static final String PSC_STATEMENT_ID_RAW = "3000000002";
    private static final String COMPANY_NUMBER = "09950914";
    private static final String ENCODED_ID = "encodedId";
    private final TestHelper testHelper = new TestHelper();
    private PscStatementDeltaProcessor deltaProcessor;
    private PscStatementDeltaDeserialiser deltaDeserialiser;

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private PscStatementApiTransformer transformer;
    @Mock
    private MapperUtils mapperUtils;

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        deltaDeserialiser = new PscStatementDeltaDeserialiser(mapper);
        deltaProcessor = new PscStatementDeltaProcessor(transformer, deltaDeserialiser, apiClientService, mapperUtils);
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid ChsDelta is given")
    void When_ValidChsDeltaMessage_Expect_ProcessorDoesNotThrow_CallsTransformer()
            throws IOException {
        // given
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(false);
        CompanyPscStatement mockCompanyPscStatement = new CompanyPscStatement();
        mockCompanyPscStatement.setPscStatementIdRaw(PSC_STATEMENT_ID_RAW);
        mockCompanyPscStatement.setCompanyNumber(COMPANY_NUMBER);

        when(mapperUtils.encode(PSC_STATEMENT_ID_RAW)).thenReturn(ENCODED_ID);
        when(transformer.transform(any(PscStatement.class))).thenReturn(mockCompanyPscStatement);

        // when
        deltaProcessor.processDelta(mockChsDeltaMessage);

        // then
        verify(transformer).transform(any(PscStatement.class));
        verify(mapperUtils).encode(PSC_STATEMENT_ID_RAW);
        verify(apiClientService).invokePscStatementPutRequest(COMPANY_NUMBER, ENCODED_ID, mockCompanyPscStatement);
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid delete ChsDelta is given")
    void When_ValidChsDeleteDeltaMessage_Expect_ProcessorDoesNotThrow() throws IOException {
        // given
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(true);
        when(mapperUtils.encode(PSC_STATEMENT_ID_RAW)).thenReturn(ENCODED_ID);

        // when
        deltaProcessor.processDeleteDelta(mockChsDeltaMessage);

        // then
        verify(mapperUtils).encode(PSC_STATEMENT_ID_RAW);
        verify(apiClientService).invokePscStatementDeleteRequest(COMPANY_NUMBER, ENCODED_ID, "20230724093435661593");
    }
}
