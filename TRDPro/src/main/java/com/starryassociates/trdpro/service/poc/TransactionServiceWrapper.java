package com.starryassociates.trdpro.service.poc;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.starryassociates.core.config.CustomLogger;
import com.starryassociates.core.config.ServiceLocator;
import com.starryassociates.trdpro.repository.poc.TransactionInfoRepo;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.Map;

public class TransactionServiceWrapper implements RequestHandler<Map<String, String>, String> {

    private final TransactionService transactionService;

    public TransactionServiceWrapper() {
        // Initialize the TransactionService with dependencies
        TransactionInfoRepo transactionInfoRepo = new TransactionInfoRepo();
        SqsClient sqsClient = SqsClient.builder().build();
        ServiceLocator serviceLocator = ServiceLocator.getInstance();
        String queueUrl = String.valueOf(serviceLocator.getConfigManager().getTransactionQueueUrl());
        CustomLogger log = serviceLocator.getLogger(this.getClass());

        // Pass dependencies to the TransactionService
        this.transactionService = new TransactionService(transactionInfoRepo, sqsClient, queueUrl, log);
    }

    @Override
    public String handleRequest(Map<String, String> input, Context context) {
        return transactionService.handleRequest(input, context);
    }
}
