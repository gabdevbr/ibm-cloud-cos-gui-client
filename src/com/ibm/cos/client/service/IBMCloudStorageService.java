package com.ibm.cos.client.service;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.model.*;
import com.ibm.cos.client.model.FileItem;

import java.io.*;
import java.util.*;

/**
 * Implementation of CloudStorageService for IBM Cloud Object Storage
 */
public class IBMCloudStorageService implements CloudStorageService {
    
    private final AmazonS3 client;
    
    public IBMCloudStorageService(AmazonS3 client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        this.client = client;
    }
    
    @Override
    public List<String> listBuckets() throws StorageException {
        try {
            List<Bucket> buckets = client.listBuckets();
            List<String> bucketNames = new ArrayList<>();
            for (Bucket bucket : buckets) {
                bucketNames.add(bucket.getName());
            }
            return bucketNames;
        } catch (Exception e) {
            throw new StorageException("Failed to list buckets", e);
        }
    }
    
    @Override
    public List<FileItem> listObjects(String bucketName, String prefix) throws StorageException {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        
        try {
            List<FileItem> items = new ArrayList<>();
            
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName.trim())
                    .withDelimiter("/");

            if (prefix != null && !prefix.trim().isEmpty()) {
                request.withPrefix(prefix.trim());
            }

            ListObjectsV2Result result = client.listObjectsV2(request);
            
            String actualPrefix = prefix != null ? prefix.trim() : "";

            // Add folders (common prefixes)
            for (String commonPrefix : result.getCommonPrefixes()) {
                String folderName = commonPrefix.substring(actualPrefix.length());
                items.add(new FileItem(folderName, FileItem.ItemType.FOLDER, 0, null));
            }

            // Add files
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                String key = objectSummary.getKey();
                if (!key.equals(actualPrefix)) {
                    String fileName = key.substring(actualPrefix.length());
                    if (!fileName.isEmpty()) {
                        items.add(new FileItem(fileName, FileItem.ItemType.FILE, 
                            objectSummary.getSize(), objectSummary.getLastModified()));
                    }
                }
            }
            
            return items;
        } catch (Exception e) {
            throw new StorageException("Failed to list objects in bucket: " + bucketName, e);
        }
    }
    
    @Override
    public void uploadFile(String bucketName, String key, File file) throws StorageException {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File must exist");
        }
        
        try {
            client.putObject(bucketName.trim(), key.trim(), file);
        } catch (Exception e) {
            throw new StorageException("Failed to upload file: " + file.getName(), e);
        }
    }
    
    @Override
    public void downloadFile(String bucketName, String key, File targetFile) throws StorageException {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (targetFile == null) {
            throw new IllegalArgumentException("Target file cannot be null");
        }
        
        try {
            S3Object object = client.getObject(bucketName.trim(), key.trim());
            try (S3ObjectInputStream inputStream = object.getObjectContent();
                 FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            throw new StorageException("Failed to download file: " + key, e);
        }
    }
    
    @Override
    public void deleteObject(String bucketName, String key) throws StorageException {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        try {
            client.deleteObject(bucketName.trim(), key.trim());
        } catch (Exception e) {
            throw new StorageException("Failed to delete object: " + key, e);
        }
    }
    
    @Override
    public List<FileItem> searchObjectsRecursively(String bucketName, String searchTerm) throws StorageException {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        
        List<FileItem> matchingItems = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        
        try {
            // List all objects in the bucket recursively (without delimiter)
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName.trim());
            
            ListObjectsV2Result result;
            do {
                result = client.listObjectsV2(request);
                
                for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    String fileName = key.substring(key.lastIndexOf('/') + 1);
                    
                    // Check if the key or filename contains the search term (case-insensitive)
                    if (key.toLowerCase().contains(lowerSearchTerm) || 
                        fileName.toLowerCase().contains(lowerSearchTerm)) {
                        
                        matchingItems.add(new FileItem(key, FileItem.ItemType.FILE, 
                            objectSummary.getSize(), objectSummary.getLastModified()));
                    }
                }
                
                // Set continuation token for pagination
                request.setContinuationToken(result.getNextContinuationToken());
                
            } while (result.isTruncated());
            
            return matchingItems;
            
        } catch (Exception e) {
            throw new StorageException("Failed to search objects in bucket: " + bucketName, e);
        }
    }
}