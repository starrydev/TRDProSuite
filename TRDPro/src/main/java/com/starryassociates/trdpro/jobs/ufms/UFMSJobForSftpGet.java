package com.starryassociates.trdpro.jobs.ufms;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

public class UFMSJobForSftpGet implements RequestHandler<Map<String, Object>, String> {

    private static final Logger logger = LoggerFactory.getLogger(UFMSJobForSftpGet.class);

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        Random random = new Random();
        int delayInSeconds = 30 + random.nextInt(30);  // Random delay between 60 and 300 seconds (up to 1 minutes)

        logger.info("Lambda function triggered. Waiting for {} seconds.", delayInSeconds);

        try {
            Thread.sleep(delayInSeconds * 1000L);  // Sleep for the random delay
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting", e);
            Thread.currentThread().interrupt();  // Reset the interrupted flag
        }

        logger.info("Execution completed after waiting.");

        return "Lambda execution completed after " + delayInSeconds + " seconds delay.";
    }
}
