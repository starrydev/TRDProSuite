package com.starryassociates.trdpro.handler.poc;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.starryassociates.core.config.ConfigManager;
import com.starryassociates.trdpro.config.CustomLogger;
import com.starryassociates.trdpro.config.ServiceLocator;
import com.starryassociates.trdpro.model.poc.TransactionInfo;
import com.starryassociates.trdpro.util.AppUtil;
import com.starryassociates.trdpro.util.SaveToS3;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TransactionFileWriterLambda {
    private final CustomLogger log;

    // Constructor is for JUnit testing only
    public TransactionFileWriterLambda(CustomLogger log) {
        this.log = log;
    }
    public TransactionFileWriterLambda() {
        this.log = ServiceLocator.getInstance().getLogger(this.getClass());
    }

    public void handleSQSEvent(SQSEvent event, Context context) {
        ConfigManager configManager = ServiceLocator.getInstance().getConfigManager();
        SqsClient sqsClient = SqsClient.builder().build(); // Create SQS client

        event.getRecords().forEach(message -> {
            try {
                // Deserialize the message body into transaction info
                String payload = message.getBody();
                List<TransactionInfo> transactions = AppUtil.fromJson(payload, TransactionInfo.class);

                // Process the transactions (write them to a file and upload to S3)
                File tempFile = AppUtil.createTempFile("transactions");
                writeTransactionsToFile(tempFile, transactions);

                SaveToS3 saveToS3 = new SaveToS3();
                String outputFilename = saveToS3.upload(tempFile.toPath(), "UFMS/" + AppUtil.ufmsObligationFilename());
                log.info("File uploaded successfully to " + outputFilename);

                // If processing is successful, delete the message from the SQS queue
                String receiptHandle = message.getReceiptHandle();
                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                        .queueUrl(String.valueOf(configManager.getTransactionQueueUrl()))
                        .receiptHandle(receiptHandle)
                        .build();
                sqsClient.deleteMessage(deleteMessageRequest);
                log.info("SQS message deleted successfully.");
            } catch (Exception e) {
                log.error("Failed to process SQS message: " + e.getMessage());
            }
        });
    }

    private void writeTransactionsToFile(File file, List<TransactionInfo> transactionInfos) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (TransactionInfo transactionInfo : transactionInfos) {
                String transactionId = String.format("%-10s", transactionInfo.getTransactionId());
                String merchant = String.format("%-50s", transactionInfo.getMerchant());
                String date = AppUtil.formatTransactionDate(transactionInfo.getDate());
                String totalAmount = AppUtil.formatAmount(transactionInfo.getTotalAmount());

                Map<String, Double> totalsByAccountCode = transactionInfo.getTransactionTotalForAcctCode();
                for (Map.Entry<String, Double> entry : totalsByAccountCode.entrySet()) {
                    String accountCode = String.format("%-10s", entry.getKey());
                    String accountTotalAmount = AppUtil.formatAmount(entry.getValue());

                    String fixedLengthRecord = transactionId + merchant + date + totalAmount + accountCode + accountTotalAmount;
                    writer.write(fixedLengthRecord);
                    writer.newLine();
                }
            }
        }catch (IOException e) {
            log.error("Error writing transaction file to S3: " + file.getName() + e.getMessage());
            throw e;
        }
    }
}
