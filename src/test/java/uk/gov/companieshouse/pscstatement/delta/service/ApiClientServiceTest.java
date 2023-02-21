package uk.gov.companieshouse.pscstatement.delta.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsDelete;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsPut;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ApiClientServiceTest {

  private final String contextId = "testContext";
  private final String companyNumber = "test12345";
  private final String statementId = "testId123456";

  private final String uri = "/company/%s/persons-with-significant-control-statements/%s/internal";

  private ApiClientService apiClientService;

  @Mock
  private Logger logger;

  @Mock
  private ApiResponse<Void> apiResponse;

  @Mock
  ResponseHandler<PscStatementsDelete> deleteResponseHandler;

  @Mock
  CompanyPscStatement statement;

  @BeforeEach
  public void setUp(){
    apiClientService = new ApiClientService(logger);
    ReflectionTestUtils.setField(apiClientService, "apiKey", "testKey");
    ReflectionTestUtils.setField(apiClientService, "url", "http://localhost:8888");
  }

  @Test
  public void returnOkResponseWhenValidPutRequestSentToApi(){
    String expectedUri = String.format(uri, companyNumber, statementId);
    ResponseHandler<PscStatementsPut> putResponseHandler = Mockito.mock(ResponseHandler.class);
    ReflectionTestUtils.setField(apiClientService, "responseHandler", putResponseHandler);
    when(putResponseHandler.handleApiResponse(any(),anyString(),
            anyString(),anyString(),
            any(PscStatementsPut.class))).thenReturn(apiResponse);

    ApiResponse<Void> actualResponse = apiClientService.invokePscStatementPutHandler(contextId, companyNumber, statementId, statement);

    assertEquals(apiResponse, actualResponse);
    verify(putResponseHandler).handleApiResponse(any(), 
      eq("testContext"), eq("putPscStatement"), eq(expectedUri), any(PscStatementsPut.class));
  }

  @Test
  public void returnOkResponseWhenValidDeleteRequestSentToApi() {
    String expectedUri = String.format(uri, companyNumber, statementId);
    when(deleteResponseHandler.handleApiResponse(any(),anyString(),
            anyString(),anyString(),
            any(PscStatementsDelete.class))).thenReturn(apiResponse);

    ApiResponse<Void> actualResponse = apiClientService.invokePscStatementDeleteHandler(contextId, companyNumber, statementId);

    assertEquals(apiResponse, actualResponse);
    verify(deleteResponseHandler).handleApiResponse(any(), eq("testContext"),
            eq("deletePscStatement"), eq(expectedUri),
            any(PscStatementsDelete.class));
  }
}


