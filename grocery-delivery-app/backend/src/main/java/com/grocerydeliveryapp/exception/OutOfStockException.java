package com.grocerydeliveryapp.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }

    public OutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutOfStockException(String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("Product '%s' is out of stock. Requested: %d, Available: %d",
                productName, requestedQuantity, availableQuantity));
    }
}
