package com.ibm.cos.client.auth;

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.auth.BasicAWSCredentials;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cos.client.model.Credentials;
import com.ibm.cos.client.model.ConnectionConfig;

/**
 * Implementation of authentication service for IBM Cloud Object Storage
 */
public class IBMAuthenticationService implements AuthenticationService {
    
    @Override
    public AmazonS3 authenticate(Credentials credentials, ConnectionConfig config) throws AuthenticationException {
        try {
            AWSCredentials awsCredentials = new BasicAWSCredentials(
                credentials.getAccessKey(), 
                credentials.getSecretKey()
            );
            
            ClientConfiguration clientConfig = new ClientConfiguration()
                    .withRequestTimeout(config.getRequestTimeout());

            AmazonS3 client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withEndpointConfiguration(new EndpointConfiguration(config.getEndpoint(), config.getRegion()))
                    .withPathStyleAccessEnabled(true)
                    .withClientConfiguration(clientConfig)
                    .build();
                    
            return client;
        } catch (Exception e) {
            throw new AuthenticationException("Failed to create authenticated client", e);
        }
    }
    
    @Override
    public boolean validateConnection(AmazonS3 client) throws AuthenticationException {
        try {
            client.listBuckets();
            return true;
        } catch (Exception e) {
            throw new AuthenticationException("Connection validation failed", e);
        }
    }
}