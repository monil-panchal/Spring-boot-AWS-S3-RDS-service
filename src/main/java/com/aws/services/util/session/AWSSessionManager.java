package com.aws.services.util.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

/**
 * Utility class for creating a singleton object of AwsSessionCredentials.
 *
 * @see AwsSessionCredentials
 */
@Component
public class AWSSessionManager {

    private Logger logger = LoggerFactory.getLogger(AWSSessionManager.class);

    @Value("${aws.access.key.id}")
    private String AwsAccessKeyId;

    @Value("${aws.access.secret.key}")
    private String AwsAccessSecretKey;

    @Value("${aws.access.session.token}")
    private String AwsAccessSessionToken;

    // Creating AWS session using key id, secretkey, and session token
    public AwsSessionCredentials createAWSSession() {
        AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials.create(AwsAccessKeyId, AwsAccessSecretKey, AwsAccessSessionToken);
        logger.info(String.format("AWS session is created: %s", awsSessionCredentials));
        return awsSessionCredentials;
    }
}