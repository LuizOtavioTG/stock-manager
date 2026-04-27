package com.luizotg.stock_manager.exception;

public class DuplicateInventoryException extends DuplicateResourceException {

    public DuplicateInventoryException(String message) {
        super(message);
    }
}
