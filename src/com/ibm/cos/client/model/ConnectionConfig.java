package com.ibm.cos.client.model;

/**
 * Configuration for connecting to IBM Cloud Object Storage
 */
public class ConnectionConfig {
    private final String endpoint;
    private final String region;
    private final int requestTimeout;
    
    public ConnectionConfig(String endpoint, String region) {
        this(endpoint, region, 5000);
    }
    
    public ConnectionConfig(String endpoint, String region, int requestTimeout) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Endpoint cannot be null or empty");
        }
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("Region cannot be null or empty");
        }
        if (requestTimeout <= 0) {
            throw new IllegalArgumentException("Request timeout must be positive");
        }
        
        this.endpoint = endpoint.trim();
        this.region = region.trim();
        this.requestTimeout = requestTimeout;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public String getRegion() {
        return region;
    }
    
    public int getRequestTimeout() {
        return requestTimeout;
    }
}