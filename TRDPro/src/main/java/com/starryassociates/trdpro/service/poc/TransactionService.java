package com.starryassociates.trdpro.service.poc;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starryassociates.core.config.CustomLogger;
import com.starryassociates.trdpro.model.poc.TransactionInfo;
import com.starryassociates.trdpro.repository.poc.TransactionInfoRepo;
import com.starryassociates.trdpro.util.AppUtil;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;
import java.util.Map;

public class TransactionService {
    private final TransactionInfoRepo transactionInfoRepo;
    private final SqsClient sqsClient;
    private final String queueUrl;  // SQS queue URL
    private final CustomLogger log;

    public TransactionService(TransactionInfoRepo transactionInfoRepo, SqsClient sqsClient, String queueUrl, CustomLogger log) {
        this.transactionInfoRepo = transactionInfoRepo;
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.log = log;
    }

    public String handleRequest(Map<String, String> input, Context context) {
        try {
            List<TransactionInfo> transactions = transactionInfoRepo.findPendingTransactions();
            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(transactions);
            AppUtil.addMessageToQueue(queueUrl, payload);
            log.info("Transaction message sent to SQS queue successfully");
            return "Transaction message sent to SQS queue successfully";

        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
            return "Failed to send transaction message to SQS";
        }
    }
}
