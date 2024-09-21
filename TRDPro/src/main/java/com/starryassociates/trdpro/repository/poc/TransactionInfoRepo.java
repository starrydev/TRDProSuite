package com.starryassociates.trdpro.repository.poc;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.starryassociates.trdpro.config.CustomLogger;
import com.starryassociates.trdpro.config.ServiceLocator;
import com.starryassociates.trdpro.exception.TransactionException;
import com.starryassociates.trdpro.model.poc.TransactionInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionInfoRepo {

    private static final String TRANSACTIONS_TABLE_NAME = "TransactionInfo";
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<TransactionInfo> transactionTable;
    private final CustomLogger log;

    public TransactionInfoRepo() {
        this.dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ServiceLocator.getInstance().getDynamoDBClient())
                .build();
        this.transactionTable = dynamoDbEnhancedClient.table(TRANSACTIONS_TABLE_NAME, TableSchema.fromBean(TransactionInfo.class));
        this.log = ServiceLocator.getInstance().getLogger(this.getClass());
    }

    public void saveTransaction(TransactionInfo transactionInfo) throws TransactionException.BaseTransactionException {
        try {
            // Validate the transactionInfo before saving
            transactionInfo.validateTransactionTotal();
            transactionInfo.setInsertDateTime(LocalDateTime.now());

            // Save the transactionInfo using the enhanced client
            transactionTable.putItem(transactionInfo);

        } catch (Exception e) {
            throw new TransactionException.BaseTransactionException("Failed to save transactionInfo: " + e.getMessage(), e);
        }
    }

    public List<TransactionInfo> findPendingTransactions() throws Exception {
        List<TransactionInfo> transactionInfos = new ArrayList<>();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();

        try {
            // Scan the table and iterate over the pages
            Iterable<Page<TransactionInfo>> pages = transactionTable.scan(scanRequest);

            for (Page<TransactionInfo> page : pages) {
                page.items().forEach(transactionInfo -> {
                    try {
                        if (transactionInfo.getTransactionItems() != null && !transactionInfo.getTransactionItems().isEmpty()) {
                            transactionInfo.validateTransactionTotal();
                            transactionInfos.add(transactionInfo);
                        } else {
                            log.warn("Transaction items are missing or empty.");
                        }
                    } catch (Exception e) {
                        log.error("Failed to deserialize or validate transaction: " + e.getMessage());
                    }
                });
            }

        } catch (Exception e) {
            log.error("Error scanning the DynamoDB table: " + e.getMessage());
            throw e;
        }

        return transactionInfos;
    }
}
