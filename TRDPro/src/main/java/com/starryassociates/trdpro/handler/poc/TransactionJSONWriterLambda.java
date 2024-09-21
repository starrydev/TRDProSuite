package com.starryassociates.trdpro.handler.poc;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starryassociates.trdpro.model.poc.TransactionInfo;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TransactionJSONWriterLambda implements RequestHandler<List<TransactionInfo>, String> {

    private static final String BUCKET_NAME = "s3-app-build-982081072770";
    private static final String OUTPUT_FILE_NAME = "transaction_output.json";

    @Override
    public String handleRequest(List<TransactionInfo> transactionInfos, Context context) {
        // Create an S3 client using AWS SDK v2
        S3Client s3Client = S3Client.builder().build();
        ObjectMapper objectMapper = new ObjectMapper();

        File tempFile = null;
        try {
            // Create a temporary file
            tempFile = File.createTempFile("transaction", ".json");

            // Write transaction data to the file in JSON format
            objectMapper.writeValue(tempFile, transactionInfos);

            // Convert the file to a byte array for upload
            byte[] fileContent = Files.readAllBytes(tempFile.toPath());

            // Create a PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(OUTPUT_FILE_NAME)
                    .build();

            // Upload the file to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));

            context.getLogger().log("File uploaded successfully to " + BUCKET_NAME + "/" + OUTPUT_FILE_NAME);

            return "File uploaded successfully to " + BUCKET_NAME + "/" + OUTPUT_FILE_NAME;

        } catch (IOException e) {
            context.getLogger().log("Error: " + e.getMessage());
            return "Failed to process transactionInfos";
        } finally {
            // Cleanup the temporary file
            tempFile.delete();
        }
    }
}
