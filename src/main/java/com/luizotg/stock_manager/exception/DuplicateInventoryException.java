package com.luizotg.stock_manager.exception;

import org.springframework.http.HttpStatus;

public class DuplicateInventoryException extends BusinessException {

    public DuplicateInventoryException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_INVENTORY");
    }
}
