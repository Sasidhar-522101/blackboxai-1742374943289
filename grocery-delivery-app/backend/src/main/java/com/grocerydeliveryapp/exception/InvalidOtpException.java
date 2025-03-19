package com.grocerydeliveryapp.exception;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }

    public InvalidOtpException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidOtpException(String email, String reason) {
        super(String.format("Invalid OTP for email '%s'. Reason: %s", email, reason));
    }

    public static InvalidOtpException expired(String email) {
        return new InvalidOtpException(email, "OTP has expired");
    }

    public static InvalidOtpException incorrect(String email) {
        return new InvalidOtpException(email, "Incorrect OTP provided");
    }

    public static InvalidOtpException notFound(String email) {
        return new InvalidOtpException(email, "No OTP request found");
    }
}
