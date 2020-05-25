package com.aws.services.service;

import com.aws.services.model.ResponseDTO;
import com.aws.services.util.client.S3ClientBuilder;
import com.aws.services.util.session.AWSSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * S3 service layer for this application
 */
@Service
public class S3Service {

    private Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private AWSSessionManager awsSessionManager;

    @Autowired
    private S3ClientBuilder clientBuilder;

    @Autowired
    private ResponseDTO responseDTO;

    private AwsSessionCredentials credentials;
    private Optional<S3Client> client;


    /**
     * Initializing AwsSessionCredentials and S3Client object after the bean creation.
     * These connection objects are singleton and will be reused during the application scope.
     */
    @PostConstruct
    public void init() throws Exception {
        /**
         * Get AwsSessionCredentials object from custom AWSSessionManager Singleton bean
         * {@link AWSSessionManager )}
         * @see AwsSessionCredentials, which is a part of AWS Java SDK.
         */
        credentials = awsSessionManager.createAWSSession();

        /**
         * Getting S3Client client from a builder method of this class.
         * {@link #getS3ClientBuilder(AwsSessionCredentials)}}
         */
        client = clientBuilder.getS3Client(credentials);
        if (client.isEmpty()) {
            throw new Exception("Cannot connect to S3 bucket");
        }
    }

    /**
     * Service method for uploading file to an existing S3 bucket.
     */
    public ResponseDTO uploadFileToS3Bucket(String fileDirectory, String filename, String bucketName, String key) {
        try {
            /**
             * Building PutObjectRequest {@link PutObjectRequest} for updating object in S3 bucket.
             */
            PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key).build();

            /**
             * Adding the S3bucket PutObjectRequest to the client for uploading the file in the S3 bucket.
             */
            client.get().putObject(request, Paths.get(fileDirectory, filename));

            String resMessage = String.format("File: %s has been successfully uploaded to S3 bucket: %s with a tag/name: %s", filename, bucketName, key);
            logger.info(resMessage);

            return new ResponseDTO(true, resMessage, null);

        } catch (Exception e) {
            String errMessage = String.format("Error in uploading the file : %s to the S3 bucket: %s", filename, bucketName);
            logger.error(errMessage);
            e.printStackTrace();
            return new ResponseDTO(false, errMessage, e.getLocalizedMessage());
        }
    }

    /**
     * Service method for creating new S3 bucket
     */
    public ResponseDTO createS3Bucket(String bucketName) {
        try {
            // Bucket creation
            client.get().createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());

            // Setting the permissions to restrict all public access on bucket and objects
            client.get().putPublicAccessBlock(PutPublicAccessBlockRequest.builder()
                    .bucket(bucketName)
                    .publicAccessBlockConfiguration(PublicAccessBlockConfiguration.builder()
                            .restrictPublicBuckets(true)
                            .ignorePublicAcls(true)
                            .blockPublicPolicy(true)
                            .build())
                    .build());


            /** The following code blocks assigned READ only permission to the owner of the bucket.
             * Following are the sequence of operations performed to modify the ACL permissions of the newly created bucket
             *
             * 1) Fetch OwnerId and displayName from the default ACL {@link GetBucketAclResponse}
             * 2) Create Grantee object {@link Grantee} with the OwnerId, displayName, and type
             * 3) Create Grant object {@link Grant} with the Grantee object reference and appropriate permissions.
             * In our case, with READ. This will not allow the owner to create new objects in the bucket.
             *
             */

            // Fetching the default bucket ACL
            GetBucketAclResponse getBucketAclResponse = client.get().getBucketAcl(GetBucketAclRequest.builder()
                    .bucket(bucketName)
                    .build());

            // Fetching the owner details
            String ownerID = getBucketAclResponse.owner().id();
            String displayName = getBucketAclResponse.owner().displayName();

            // Creating Grantee object with the owner details
            Grantee grantee = Grantee.builder().id(ownerID).type(Type.CANONICAL_USER).displayName(displayName).build();

            // Creating Grant object with the owner details and the permission READ
            Grant grant = Grant.builder().grantee(grantee).permission(Permission.READ).build();

            // Updating the AccessControlPolicy of the bucket with the above created.
            client.get().putBucketAcl(PutBucketAclRequest.builder()
                    .bucket(bucketName)
                    .accessControlPolicy(AccessControlPolicy.builder()
                            .owner(Owner.builder()
                                    .id(ownerID)
                                    .displayName(displayName)
                                    .build())
                            .grants(grant)
                            .build())
                    .build());

            String resMessage = String.format("New S3 bucket with name: %s is successfully created.", bucketName);
            logger.info(resMessage);

            return new ResponseDTO(true, resMessage, null);

        } catch (Exception e) {

            String errMessage = String.format("Error in creating new S3 bucket:", bucketName);
            logger.error(errMessage);
            e.printStackTrace();

            return new ResponseDTO(false, errMessage, e.getLocalizedMessage());
        }
    }

    /**
     * Service method for copying object from source to destination bucket.
     */
    public ResponseDTO copyObject(String sourceBucket, String destinationBucket, String objectKey) {
        try {
            // encoded url of the source bucket
            String sourceBucketEncodedUrl = URLEncoder.encode(sourceBucket + "/" + objectKey, StandardCharsets.UTF_8.toString());

            // CopyObjectRequest object for creating a copy object request
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .copySource(sourceBucketEncodedUrl)
                    .destinationBucket(destinationBucket)
                    .destinationKey(objectKey)
                    .build();

            // submitting the CopyObjectRequest to the S3Client
            client.get().copyObject(copyObjectRequest);

            String resMessage = String.format("Object: %s is successfully copied from source: %s to destination bucket %s", objectKey, sourceBucket, destinationBucket);
            logger.info(resMessage);

            return new ResponseDTO(true, resMessage, null);

        } catch (Exception e) {

            String errMessage = String.format("Error in copying the object: %s from source %s to destination bucket %s:", objectKey, sourceBucket, destinationBucket);
            logger.error(errMessage);
            e.printStackTrace();

            return new ResponseDTO(false, errMessage, e.getLocalizedMessage());

        }
    }

    /**
     * Service method for fetching object from S3 bucket
     */
    public Optional<ResponseInputStream> getObject(String bucketName, String key) {

        Optional<ResponseInputStream> responseInputStream = Optional.empty();

        try {
            // GetObjectRequest object for fetching object from the bucket.
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // Fetching the object from S3client
            responseInputStream = Optional.of(client.get().getObject(objectRequest));
            logger.info(String.format("Object: %s fetched successfully from bucket: %s", key, bucketName));

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("Error in fetching object: %s from bucket: %s", key, bucketName));
            throw e;
        }
        return responseInputStream;
    }
}