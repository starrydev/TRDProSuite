package com.starryassociates.trdpro.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class AppUtil {

    public static File createTempFile(String prefix) throws IOException {
        return File.createTempFile(prefix+generateUniqueName(), ".tmp");
    }

    public static String generateUniqueName() {
        return formatDateForNow() + "-" + UUID.randomUUID().toString();
    }

    public static void addMessageToQueue(String queueUrl, String messageBody) {
        SqsClient sqsClient = SqsClient.builder().build();
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();
        sqsClient.sendMessage(sendMessageRequest);
    }

    private static String formatDateForNow(){
        ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return now.format(formatter);
    }

    public static String formatTransactionDate(String date) {
        date = date.replace("-", "");
        return date.substring(4, 6) + date.substring(6) + date.substring(0, 4);
    }

    public static String formatAmount(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("00000000000000000000");
        return decimalFormat.format(amount * 100);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Converts a Java object to a JSON string
    public static String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    // Generic method to convert a JSON string to a Java object (list of any type)
    public static <T> List<T> fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        // Use Jackson to deserialize JSON into a List of the given type
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static String ufmsObligationFilename(){
        return "HHS_ETRAVEL_PO_TRA_PSC_" + formatDateForNow() + ".DAT";
    }
}
