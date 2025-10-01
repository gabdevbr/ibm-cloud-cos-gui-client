package com.ibm.cos.client.operation;

/**
 * Interface for listening to file operation events.
 * Follows Interface Segregation Principle by separating operation listeners.
 */
public interface FileOperationListener {
    void onOperationStarted(String message);
    void onOperationProgress(String message);
    void onOperationCompleted(String message);
    void onOperationFailed(String message);
}