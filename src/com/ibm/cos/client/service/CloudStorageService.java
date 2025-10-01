package com.ibm.cos.client.service;

import com.ibm.cos.client.model.FileItem;
import java.io.File;
import java.util.List;

/**
 * Service for interacting with cloud object storage
 */
public interface CloudStorageService {
    
    /**
     * Lists all available buckets
     * @return list of bucket names
     * @throws StorageException if operation fails
     */
    List<String> listBuckets() throws StorageException;
    
    /**
     * Lists objects in a bucket with optional prefix filtering
     * @param bucketName the bucket name
     * @param prefix the prefix to filter by (can be null or empty)
     * @return list of file items
     * @throws StorageException if operation fails
     */
    List<FileItem> listObjects(String bucketName, String prefix) throws StorageException;
    
    /**
     * Uploads a file to the specified bucket and key
     * @param bucketName the target bucket
     * @param key the object key
     * @param file the file to upload
     * @throws StorageException if upload fails
     */
    void uploadFile(String bucketName, String key, File file) throws StorageException;
    
    /**
     * Downloads an object to a local file
     * @param bucketName the source bucket
     * @param key the object key
     * @param targetFile the target local file
     * @throws StorageException if download fails
     */
    void downloadFile(String bucketName, String key, File targetFile) throws StorageException;
    
    /**
     * Deletes an object from the bucket
     * @param bucketName the bucket name
     * @param key the object key
     * @throws StorageException if deletion fails
     */
    void deleteObject(String bucketName, String key) throws StorageException;
    
    /**
     * Searches for objects recursively in a bucket that contain the search term
     * @param bucketName the bucket name
     * @param searchTerm the term to search for in object keys
     * @return list of file items that match the search term
     * @throws StorageException if search fails
     */
    List<FileItem> searchObjectsRecursively(String bucketName, String searchTerm) throws StorageException;
}