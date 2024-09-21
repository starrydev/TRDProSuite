package com.starryassociates.trdpro.poc;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class MockLambdaContext implements Context {

    @Override
    public String getAwsRequestId() {
        return "mockRequestId";
    }

    @Override
    public String getLogGroupName() {
        return "mockLogGroupName";
    }

    @Override
    public String getLogStreamName() {
        return "mockLogStreamName";
    }

    @Override
    public String getFunctionName() {
        return "mockFunctionName";
    }

    @Override
    public String getFunctionVersion() {
        return "mockFunctionVersion";
    }

    @Override
    public String getInvokedFunctionArn() {
        return "mockInvokedFunctionArn";
    }

    @Override
    public CognitoIdentity getIdentity() {
        return null;
    }

    @Override
    public ClientContext getClientContext() {
        return null;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return 300000; // Mock time remaining
    }

    @Override
    public int getMemoryLimitInMB() {
        return 512; // Mock memory limit
    }

    @Override
    public LambdaLogger getLogger() {
        return new MockLambdaLogger();
    }
}

class MockLambdaLogger implements LambdaLogger {
    @Override
    public void log(String message) {
        System.out.println(message);
    }

    @Override
    public void log(byte[] message) {
        System.out.println(new String(message));
    }
}
