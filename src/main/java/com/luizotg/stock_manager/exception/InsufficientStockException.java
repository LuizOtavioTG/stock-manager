package com.luizotg.stock_manager.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK");
    }
}
