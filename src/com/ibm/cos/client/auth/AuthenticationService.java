package com.ibm.cos.client.auth;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cos.client.model.Credentials;
import com.ibm.cos.client.model.ConnectionConfig;

/**
 * Service responsible for handling authentication with IBM Cloud Object Storage
 */
public interface AuthenticationService {
    
    /**
     * Authenticates and creates a client connection
     * @param credentials the authentication credentials
     * @param config the connection configuration
     * @return authenticated AmazonS3 client
     * @throws AuthenticationException if authentication fails
     */
    AmazonS3 authenticate(Credentials credentials, ConnectionConfig config) throws AuthenticationException;
    
    /**
     * Validates the connection by performing a test operation
     * @param client the client to validate
     * @return true if connection is valid
     * @throws AuthenticationException if validation fails
     */
    boolean validateConnection(AmazonS3 client) throws AuthenticationException;
}