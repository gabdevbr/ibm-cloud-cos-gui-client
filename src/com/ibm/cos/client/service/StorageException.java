package com.ibm.cos.client.service;

/**
 * Exception thrown when storage operations fail
 */
public class StorageException extends Exception {
    
    public StorageException(String message) {
        super(message);
    }
    
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}