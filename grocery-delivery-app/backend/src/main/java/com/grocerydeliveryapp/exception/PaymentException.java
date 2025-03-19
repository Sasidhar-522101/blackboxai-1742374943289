package com.grocerydeliveryapp.exception;

public class PaymentException extends RuntimeException {
    private final String transactionId;
    private final PaymentErrorType errorType;

    public PaymentException(String message) {
        super(message);
        this.transactionId = null;
        this.errorType = PaymentErrorType.GENERAL_ERROR;
    }

    public PaymentException(String message, String transactionId, PaymentErrorType errorType) {
        super(message);
        this.transactionId = transactionId;
        this.errorType = errorType;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.transactionId = null;
        this.errorType = PaymentErrorType.GENERAL_ERROR;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public PaymentErrorType getErrorType() {
        return errorType;
    }

    public enum PaymentErrorType {
        INVALID_CARD,
        INSUFFICIENT_FUNDS,
        PAYMENT_DECLINED,
        INVALID_UPI,
        UPI_TIMEOUT,
        TRANSACTION_FAILED,
        PAYMENT_GATEWAY_ERROR,
        GENERAL_ERROR
    }

    // Factory methods for common payment errors
    public static PaymentException invalidCard(String message) {
        return new PaymentException(message, null, PaymentErrorType.INVALID_CARD);
    }

    public static PaymentException insufficientFunds(String transactionId) {
        return new PaymentException(
            "Insufficient funds in the account",
            transactionId,
            PaymentErrorType.INSUFFICIENT_FUNDS
        );
    }

    public static PaymentException paymentDeclined(String transactionId, String reason) {
        return new PaymentException(
            "Payment declined: " + reason,
            transactionId,
            PaymentErrorType.PAYMENT_DECLINED
        );
    }

    public static PaymentException invalidUpi(String upiId) {
        return new PaymentException(
            "Invalid UPI ID: " + upiId,
            null,
            PaymentErrorType.INVALID_UPI
        );
    }

    public static PaymentException upiTimeout(String transactionId) {
        return new PaymentException(
            "UPI payment request timed out",
            transactionId,
            PaymentErrorType.UPI_TIMEOUT
        );
    }

    public static PaymentException transactionFailed(String transactionId, String reason) {
        return new PaymentException(
            "Transaction failed: " + reason,
            transactionId,
            PaymentErrorType.TRANSACTION_FAILED
        );
    }

    public static PaymentException gatewayError(String transactionId, String gatewayMessage) {
        return new PaymentException(
            "Payment gateway error: " + gatewayMessage,
            transactionId,
            PaymentErrorType.PAYMENT_GATEWAY_ERROR
        );
    }
}
