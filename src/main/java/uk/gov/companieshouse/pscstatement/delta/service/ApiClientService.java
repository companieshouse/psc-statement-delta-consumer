package uk.gov.companieshouse.pscstatement.delta.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsDelete;
import uk.gov.companieshouse.api.handler.delta.pscstatements.request.PscStatementsPut;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.logging.Logger;

@Service
@SuppressWarnings("unchecked")
public class ApiClientService {

    @Value("${api.psc-statements-data-api-key}")
    private String apiKey;

    @Value("${api.api-url}")
    private String url;

    private final Logger logger;

    private final ResponseHandlerFactory responseHandlerFactory;

    @Autowired
    public ApiClientService(Logger logger, ResponseHandlerFactory responseHandlerFactory) {
        this.logger = logger;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    /**
     * fetches api client.
     */
    public InternalApiClient getApiClient(String context) {
        InternalApiClient apiClient = new InternalApiClient(this.getHttpClient(context));
        apiClient.setBasePath(url);
        return apiClient;
    }

    /**
     * fetches HttpClient with context ID passed to it.
     */
    public ApiKeyHttpClient getHttpClient(String contextId) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(apiKey);
        httpClient.setRequestId(contextId);
        return httpClient;
    }

    /**
     * Invokes put handler for psc statements.
     */
    public ApiResponse<Void> invokePscStatementPutHandler(String context, String companyNumber,
            String statementId, CompanyPscStatement statement) {
        final String uri = String.format(
                "/company/%s/persons-with-significant-control-statements/%s/internal",
                companyNumber,
                statementId);
        PscStatementsPut putExecuteOp = getApiClient(context)
                .privateDeltaResourceHandler()
                .putPscStatements(uri, statement);

        Map<String, Object> logMap = createLogMap(companyNumber, statementId, "PUT", uri);
        logger.infoContext(context, String.format("PUT %s", uri), logMap);

        ResponseHandler<PscStatementsPut> responseHandler =
                (ResponseHandler<PscStatementsPut>) responseHandlerFactory.createResponseHandler(
                        putExecuteOp);
        return responseHandler.handleApiResponse(logger, context, "putPscStatement", uri,
                putExecuteOp);
    }

    /**
     * Invokes delete handler for psc statements.
     */
    public ApiResponse<Void> invokePscStatementDeleteHandler(String context, String companyNumber,
            String statementId, String deltaAt) {
        final String uri = String.format(
                "/company/%s/persons-with-significant-control-statements/%s/internal",
                companyNumber, statementId);
        PscStatementsDelete deleteExecuteOp = getApiClient(context)
                .privateDeltaResourceHandler()
                .deletePscStatements(uri, deltaAt);

        Map<String, Object> logMap = createLogMap(companyNumber, statementId, "DELETE", uri);
        logger.infoContext(context, String.format("DELETE %s", uri), logMap);
        ResponseHandler<PscStatementsDelete> responseHandler =
                (ResponseHandler<PscStatementsDelete>) responseHandlerFactory.createResponseHandler(
                        deleteExecuteOp);

        return responseHandler.handleApiResponse(logger, context, "deletePscStatement", uri,
                deleteExecuteOp);
    }

    // logMaps set to final

    /**
     * logger for request.
     */
    public Map<String, Object> createLogMap(String companyNumber, String statementId, String method,
            String path) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("company_number", companyNumber);
        logMap.put("psc_statement_id", statementId);
        logMap.put("method", method);
        logMap.put("path", path);
        return logMap;
    }
}

