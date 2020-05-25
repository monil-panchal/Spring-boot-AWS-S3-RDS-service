package com.aws.services.util.client;

import com.aws.services.util.session.AWSSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Optional;

/**
 * Utility class for creating a singleton object of S3Client.
 *
 * @see S3Client
 */
@Component
public class S3ClientBuilder {

    private Logger logger = LoggerFactory.getLogger(S3ClientBuilder.class);

    /**
     * Method for building S3Client.
     * Param - AWSSessionManager object {@link AWSSessionManager )}
     */
    public Optional<S3Client> getS3Client(AwsSessionCredentials credentials) {
        Optional<S3Client> client = Optional.empty();
        try {
            client = Optional.of(S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.US_EAST_1)
                    .build());

            logger.info("S3 Client created successfully");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in creating S3Client");
            throw e;
        }
        return client;
    }
}