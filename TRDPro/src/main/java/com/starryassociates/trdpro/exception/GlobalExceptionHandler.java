package com.starryassociates.trdpro.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public ErrorResponse handleTransactionValidationException(TransactionException.TransactionValidationException ex) {
        logger.error("TransactionInfo validation failed: {}", ex.getMessage());
        return new ErrorResponse("TRANSACTION_VALIDATION_ERROR", ex.getMessage());
    }

    public ErrorResponse handleInvalidTripTypeException(TransactionException.InvalidTripTypeException ex) {
        logger.error("Invalid TripType provided: {}", ex.getMessage());
        return new ErrorResponse("INVALID_TRIP_TYPE", ex.getMessage());
    }

    public ErrorResponse handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred: {}", ex.getMessage());
        return new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.");
    }

    // Nested class for standardized error responses
    public static class ErrorResponse {
        private String errorCode;
        private String errorMessage;

        public ErrorResponse(String errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
