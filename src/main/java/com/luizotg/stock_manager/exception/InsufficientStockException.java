package com.luizotg.stock_manager.exception;

public class InsufficientStockException extends IllegalArgumentException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
