package com.starryassociates.trdpro.poc;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starryassociates.trdpro.config.CustomLogger;
import com.starryassociates.trdpro.model.poc.TransactionInfo;
import com.starryassociates.trdpro.repository.poc.TransactionInfoRepo;
import com.starryassociates.trdpro.service.poc.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    @Mock
    private TransactionInfoRepo transactionInfoRepo;

    @Mock
    private SqsClient sqsClient;

    @Mock
    private CustomLogger log;

    @InjectMocks
    private TransactionService transactionService;

    private final String queueUrl = "https://sqs.amazonaws.com/123456789012/testQueue";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionService(transactionInfoRepo, sqsClient, queueUrl, log);
    }

/*
    @Test
    public void testHandleRequest_Success() throws Exception {
        String json = "[{\"transactionId\":\"401\",\"merchant\":\"Delta\",\"totalAmount\":109.99,\"date\":\"2024-08-18\",\"tripType\":\"ENTITLEMENT TRAVEL\",\"travellerId\":\"271356701\",\"creditCard\":\"4486120013706073\",\"transactionItems\":[{\"itemId\":\"Line1\",\"itemAmount\":50,\"description\":\"Delta\",\"accountSplits\":[{\"accountCode\":\"AC001\",\"percentage\":25},{\"accountCode\":\"AC002\",\"percentage\":75}]},{\"itemId\":\"Line2\",\"itemAmount\":59.99,\"description\":\"NYC\",\"accountSplits\":[{\"accountCode\":\"AC001\",\"percentage\":100}]}]},{\"transactionId\":\"402\",\"merchant\":\"SouthWest\",\"totalAmount\":1109.99,\"date\":\"2024-08-18\",\"tripType\":\"FT-FOREIGN TRAVEL\",\"creditCard\":\"4486120013706074\",\"transactionItems\":[{\"itemId\":\"Line1\",\"itemAmount\":1050,\"description\":\"Delta\",\"accountSplits\":[{\"accountCode\":\"AC001\",\"percentage\":45},{\"accountCode\":\"AC002\",\"percentage\":55}]},{\"itemId\":\"Line2\",\"itemAmount\":59.99,\"description\":\"NYC\",\"accountSplits\":[{\"accountCode\":\"AC001\",\"percentage\":25},{\"accountCode\":\"AC002\",\"percentage\":25},{\"accountCode\":\"AC004\",\"percentage\":50}]}]}]";
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        List<TransactionInfo> transactions = objectMapper.readValue(json, new TypeReference<List<TransactionInfo>>() {});

        when(transactionInfoRepo.findPendingTransactions()).thenReturn(transactions);

        ArgumentCaptor<SendMessageRequest> sendMessageRequestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        Map<String, String> input = new HashMap<>();
        Context context = mock(Context.class);

        // Act
        String result = transactionService.handleRequest(input, context);

        // Assert
        assertEquals("Transaction message sent to SQS queue successfully", result);
        verify(log).info("Transaction message sent to SQS queue successfully");
    }
*/

    @Test
    public void testHandleRequest_Failure() throws Exception {
        // Arrange
        when(transactionInfoRepo.findPendingTransactions()).thenThrow(new RuntimeException("Database error"));

        Map<String, String> input = new HashMap<>();
        Context context = mock(Context.class);

        // Act
        String result = transactionService.handleRequest(input, context);

        // Assert
        assertEquals("Failed to send transaction message to SQS", result);
        verify(log).error(contains("Unexpected error: Database error"));
    }
}
