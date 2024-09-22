package com.starryassociates.trdpro.handler.poc;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starryassociates.core.config.CustomLogger;
import com.starryassociates.trdpro.config.ServiceLocator;
import com.starryassociates.trdpro.exception.TransactionException.BaseTransactionException;
import com.starryassociates.trdpro.model.poc.TransactionInfo;
import com.starryassociates.trdpro.repository.poc.TransactionInfoRepo;
import com.starryassociates.trdpro.util.AppUtil;
import com.starryassociates.trdpro.util.SaveToS3;

import java.util.List;

public class TransactionProcessorLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final ServiceLocator serviceLocator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TransactionInfoRepo transactionInfoRepo;
    private final String queueUrl;  // SQS queue URL
    private final CustomLogger log;

    // Default constructor required for AWS Lambda
    public TransactionProcessorLambda() {
        this.serviceLocator = ServiceLocator.getInstance();
        this.transactionInfoRepo = new TransactionInfoRepo();
        this.log = serviceLocator.getLogger(this.getClass());
        this.queueUrl = serviceLocator.getConfigManager().getTransactionQueueUrl();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        log.info("Full request received: " + request.toString());

        if (request.getBody() == null) {
            return createResponse(400, "Request body is required");
        }
        try {
            SaveToS3 saveToS3 = new SaveToS3();
            String uniqueName = saveToS3.upload(request.getBody(),"etsnext/" + AppUtil.generateUniqueName());
            AppUtil.addMessageToQueue(queueUrl, uniqueName);
            log.info("Transaction message {} sent to SQS queue successfully", uniqueName);

            log.info("Convert the JSON payload from the request body into a list of TransactionInfo objects");
            JsonNode rootNode = objectMapper.readTree(request.getBody());
            JsonNode transactionsNode = rootNode.get("transactions");

            log.info("Convert the transactions array to a list of TransactionInfo objects");
            List<TransactionInfo> transactions = objectMapper.convertValue(
                    transactionsNode,
                    new TypeReference<List<TransactionInfo>>() {}
            );

            log.info("Process each transaction");
            for (TransactionInfo transaction : transactions) {
                transactionInfoRepo.saveTransaction(transaction);
            }
            log.info("TransactionInfo processed successfully!");
            return createResponse(200, "TransactionInfo processed successfully!");

        } catch (BaseTransactionException e) {
            log.error("TransactionInfo processing error: " + e.getMessage());
            return createResponse(400, "TransactionInfo processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
            return createResponse(500, "Failed to process transaction: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(body);
    }
}
