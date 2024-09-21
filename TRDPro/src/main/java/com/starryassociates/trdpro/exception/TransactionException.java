package com.starryassociates.trdpro.exception;

public class TransactionException {

    // Base Exception
    public static class BaseTransactionException extends RuntimeException {
        public BaseTransactionException(String message) {
            super(message);
        }

        public BaseTransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // TransactionInfo Validation Exception
    public static class TransactionValidationException extends BaseTransactionException {
        public TransactionValidationException(String message) {
            super(message);
        }

        public TransactionValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Invalid TripType Exception
    public static class InvalidTripTypeException extends IllegalArgumentException {
        public InvalidTripTypeException(String message) {
            super(message);
        }

        public InvalidTripTypeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Other custom exceptions can be added here as needed
}
