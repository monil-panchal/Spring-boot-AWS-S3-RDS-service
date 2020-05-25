package com.aws.services.util.client;

import com.aws.services.util.session.AWSSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;

import java.util.Optional;


/**
 * Utility class for creating a singleton object of RdsClient.
 *
 * @see RdsClient
 */
@Component
public class RDSClientBuilder {

    private Logger logger = LoggerFactory.getLogger(RDSClientBuilder.class);

    /**
     * Method for building AWS RDS Client .
     * Param - AWSSessionManager object {@link AWSSessionManager )}
     */
    public Optional<RdsClient> getRDSClient(AwsSessionCredentials credentials) {

        Optional<RdsClient> client = Optional.empty();
        try {
            client = Optional.of(RdsClient.builder()
                    .credentialsProvider(StaticCredentialsProvider
                            .create(credentials))
                    .region(Region.US_EAST_1)
                    .build());
            logger.info("RDS Client created successfully");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in creating RDS Client");
            throw e;
        }
        return client;
    }
}
