package com.ibm.cos.client.model;

/**
 * Represents authentication credentials for IBM Cloud Object Storage
 */
public class Credentials {
    private final String accessKey;
    private final String secretKey;
    
    public Credentials(String accessKey, String secretKey) {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Access key cannot be null or empty");
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        
        this.accessKey = accessKey.trim();
        this.secretKey = secretKey.trim();
    }
    
    public String getAccessKey() {
        return accessKey;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
}