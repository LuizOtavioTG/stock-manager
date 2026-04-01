package com.luizotg.stock_manager.exception;

import org.springframework.http.HttpStatus;

public class InvalidStockMovementException extends BusinessException {

    public InvalidStockMovementException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_STOCK_MOVEMENT");
    }
}
