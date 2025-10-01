package com.ibm.cos.client.operation;

import java.io.File;
import java.util.List;

/**
 * Interface for file operations on cloud storage.
 * Follows Interface Segregation Principle - each operation has its own interface.
 */
public interface FileOperationHandler {
    void uploadFiles(List<File> files, String bucket, String prefix, FileOperationListener listener);
    void downloadFiles(List<String> keys, String bucket, File targetDirectory, FileOperationListener listener);
    void deleteFiles(List<String> keys, String bucket, FileOperationListener listener);
}