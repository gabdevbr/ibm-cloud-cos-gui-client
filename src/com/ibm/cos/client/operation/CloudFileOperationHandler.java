package com.ibm.cos.client.operation;

import com.ibm.cos.client.service.CloudStorageService;
import com.ibm.cos.client.service.StorageException;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of FileOperationHandler for cloud storage operations.
 * Follows Single Responsibility Principle - handles only file operations.
 * Follows Dependency Inversion Principle - depends on abstractions (CloudStorageService).
 */
public class CloudFileOperationHandler implements FileOperationHandler {
    private final CloudStorageService storageService;

    public CloudFileOperationHandler(CloudStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void uploadFiles(List<File> files, String bucket, String prefix, FileOperationListener listener) {
        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() {
                AtomicInteger successCount = new AtomicInteger(0);
                
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    try {
                        publish("Uploading " + file.getName() + "... (" + (i + 1) + "/" + files.size() + ")");
                        String key = prefix + file.getName();
                        storageService.uploadFile(bucket, key, file);
                        successCount.incrementAndGet();
                    } catch (StorageException ex) {
                        publish("Failed to upload " + file.getName() + ": " + ex.getMessage());
                    }
                }
                return successCount.get();
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    listener.onOperationProgress(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    int successCount = get();
                    if (successCount == files.size()) {
                        String message = successCount == 1 ? 
                            "Upload completed: " + files.get(0).getName() :
                            "Uploaded " + successCount + " files successfully";
                        listener.onOperationCompleted(message);
                    } else {
                        String message = "Uploaded " + successCount + " of " + files.size() + " files";
                        if (successCount == 0) {
                            listener.onOperationFailed("Upload failed for all files");
                        } else {
                            listener.onOperationCompleted(message);
                        }
                    }
                } catch (Exception ex) {
                    listener.onOperationFailed("Upload error: " + ex.getMessage());
                }
            }
        };
        
        String message = files.size() == 1 ? 
            "Uploading " + files.get(0).getName() + "..." :
            "Uploading " + files.size() + " files...";
        listener.onOperationStarted(message);
        worker.execute();
    }

    @Override
    public void downloadFiles(List<String> keys, String bucket, File targetDirectory, FileOperationListener listener) {
        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() {
                AtomicInteger successCount = new AtomicInteger(0);
                
                for (int i = 0; i < keys.size(); i++) {
                    String key = keys.get(i);
                    try {
                        String fileName = key.substring(key.lastIndexOf("/") + 1);
                        publish("Downloading " + fileName + "... (" + (i + 1) + "/" + keys.size() + ")");
                        File targetFile = new File(targetDirectory, fileName);
                        storageService.downloadFile(bucket, key, targetFile);
                        successCount.incrementAndGet();
                    } catch (StorageException ex) {
                        String fileName = key.substring(key.lastIndexOf("/") + 1);
                        publish("Failed to download " + fileName + ": " + ex.getMessage());
                    }
                }
                return successCount.get();
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    listener.onOperationProgress(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    int successCount = get();
                    if (successCount == keys.size()) {
                        String message = successCount == 1 ? 
                            "Download completed" :
                            "Downloaded " + successCount + " files successfully";
                        listener.onOperationCompleted(message);
                    } else {
                        String message = "Downloaded " + successCount + " of " + keys.size() + " files";
                        if (successCount == 0) {
                            listener.onOperationFailed("Download failed for all files");
                        } else {
                            listener.onOperationCompleted(message);
                        }
                    }
                } catch (Exception ex) {
                    listener.onOperationFailed("Download error: " + ex.getMessage());
                }
            }
        };
        
        String message = keys.size() == 1 ? 
            "Downloading file..." :
            "Downloading " + keys.size() + " files...";
        listener.onOperationStarted(message);
        worker.execute();
    }

    @Override
    public void deleteFiles(List<String> keys, String bucket, FileOperationListener listener) {
        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() {
                AtomicInteger successCount = new AtomicInteger(0);
                
                for (int i = 0; i < keys.size(); i++) {
                    String key = keys.get(i);
                    try {
                        String fileName = key.substring(key.lastIndexOf("/") + 1);
                        if (fileName.isEmpty()) fileName = key;
                        publish("Deleting " + fileName + "... (" + (i + 1) + "/" + keys.size() + ")");
                        storageService.deleteObject(bucket, key);
                        successCount.incrementAndGet();
                    } catch (StorageException ex) {
                        String fileName = key.substring(key.lastIndexOf("/") + 1);
                        if (fileName.isEmpty()) fileName = key;
                        publish("Failed to delete " + fileName + ": " + ex.getMessage());
                    }
                }
                return successCount.get();
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    listener.onOperationProgress(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    int successCount = get();
                    if (successCount == keys.size()) {
                        String message = successCount == 1 ? 
                            "Delete completed" :
                            "Deleted " + successCount + " items successfully";
                        listener.onOperationCompleted(message);
                    } else {
                        String message = "Deleted " + successCount + " of " + keys.size() + " items";
                        if (successCount == 0) {
                            listener.onOperationFailed("Delete failed for all items");
                        } else {
                            listener.onOperationCompleted(message);
                        }
                    }
                } catch (Exception ex) {
                    listener.onOperationFailed("Delete error: " + ex.getMessage());
                }
            }
        };
        
        String message = keys.size() == 1 ? 
            "Deleting item..." :
            "Deleting " + keys.size() + " items...";
        listener.onOperationStarted(message);
        worker.execute();
    }
}