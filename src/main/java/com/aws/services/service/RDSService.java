package com.aws.services.service;

import com.aws.services.util.client.RDSClientBuilder;
import com.aws.services.util.connection.JDBCConnectionBuilder;
import com.aws.services.model.ResponseDTO;
import com.aws.services.util.session.AWSSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.rds.RdsClient;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * S3 service layer for this application
 */
@Service
public class RDSService {

    private Logger logger = LoggerFactory.getLogger(RDSService.class);

    @Autowired
    private AWSSessionManager awsSessionManager;

    @Autowired
    private RDSClientBuilder clientBuilder;

    @Autowired
    private JDBCConnectionBuilder jdbcConnectionBuilder;

    @Autowired
    private S3Service s3Service;

    private AwsSessionCredentials credentials;
    private Optional<RdsClient> client;
    private Optional<Connection> connection;

    /**
     * Initializing AwsSessionCredentials, RdsClient, and JDBC connection object after the creation of this class.
     * These connection objects are singleton and will be resused during the application scope.
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
        client = clientBuilder.getRDSClient(credentials);
        if (client.isEmpty()) {
            throw new Exception("Cannot connect to RDS");
        }

        connection = jdbcConnectionBuilder.createDBConnection();
        if (connection.isEmpty()) {
            throw new Exception("Cannot connect to DB");
        }
    }

    /**
     * Service method for inserting encrypted user credentials to AWS RDS.
     * The encryption is done using an external lookup table which is hosted in AWS S3 bucket.
     */
    public ResponseDTO insertUserRecord(String username, String password) throws Exception {

        try {
            //fetch the lookup table from S3 bucket
            Optional<ResponseInputStream> responseInputStream = fetchLookUpTableObjectFromS3Bucket();

            if (responseInputStream.isEmpty())
                throw new Exception("Unable to fetch lookup table from S3 bucket.");

            // Getting HashMap from the Lookup table object.
            // This map will be used to construct an encrypted password.
            Map<String, String> lookUpTableMap = getMapFromLookUpTable(responseInputStream.get(), "encrypt");
            if (lookUpTableMap == null || lookUpTableMap.size() == 0)
                throw new Exception("LookupTable is empty. Unable to construct HashMap from the lookup table.");


            // SQL query for inserting user record
            String insertQuery = "INSERT INTO User(username, password)  values (?,?)";
            PreparedStatement preparedStatement = connection.get().prepareStatement(insertQuery);

            StringBuilder encryptedPassword = new StringBuilder();
            if ((!username.isEmpty() && username != null)
                    && (!password.isEmpty() && password != null && password.matches("\\p{javaLowerCase}+"))) {

                for (int i = 0; i < password.length(); i++) {
                    String encryptedToken = lookUpTableMap.get(String.valueOf(password.charAt(i)));
                    encryptedPassword.append(encryptedToken);
                }
            } else {
                throw new Exception("Username or password not valid.");
            }

            logger.info(String.format("User: %s original password: %s", username, password));
            logger.info(String.format("User: %s encrypted password: %s", username, encryptedPassword));

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, encryptedPassword.toString());

            // Executing the query to store the user record

            int row = preparedStatement.executeUpdate();
            // check if the record was inserted successfully
            if (row > 0) {
                String successString = String.format("User: %s record with password: %s has been successfully inserted in the DB", username, password);
                logger.info(successString);

                return new ResponseDTO(true, successString, null);

            } else {
                String failureString = String.format("Failed to insert User: %s record in the DB", username);
                logger.error(failureString);
                throw new Exception(failureString);

            }

        } catch (Exception e) {

            String errMessage = String.format("Error in inserting the user record");
            logger.error(errMessage);
            e.printStackTrace();

            return new ResponseDTO(false, errMessage, e.getLocalizedMessage());
        }
    }

    /**
     * Service method for retrieving user credentials from AWS RDS.
     * The decryption is done using an external lookup table which is hosted in AWS S3 bucket.
     */
    public ResponseDTO getUserRecord(String username) throws Exception {

        try {
            //fetch the lookup table from S3 bucket
            Optional<ResponseInputStream> responseInputStream = fetchLookUpTableObjectFromS3Bucket();

            if (responseInputStream.isEmpty())
                throw new Exception("Unable to fetch lookup table from S3 bucket.");

            // Getting HashMap from the Lookup table object.
            // This map will be used to decrypt the user password.
            Map<String, String> lookUpTableMap = getMapFromLookUpTable(responseInputStream.get(), "decrypt");

            // SQL query for fetching the user record based on username
            String selectQuery = "SELECT * FROM User WHERE username = " + username;
            Statement statement = connection.get().createStatement();

            StringBuilder encryptedPassword = new StringBuilder();
            if ((!username.isEmpty() && username != null)) {

                ResultSet resultSet = statement.executeQuery(selectQuery);

                if (!resultSet.isBeforeFirst()) {
                    logger.error(String.format("User: %s is not found in the database", username));
                    throw new Exception(String.format("User: %s is not found in the database", username));
                }

                while (resultSet.next()) {
                    logger.info(String.format("User data retrieved successfully"));
                    String user = resultSet.getString(1);
                    String password = resultSet.getString(2);

                    logger.info(String.format("User: %s encrypted password stored in DB: %s", username, password));

                    //Decrypting the password using the map from lookup table.
                    for (int i = 0; i < password.length() - 1; i++) {
                        if (i % 2 != 0)
                            continue;

                        String decryptedToken = lookUpTableMap.get(password.charAt(i) + "" + password.charAt(i + 1));
                        encryptedPassword.append(decryptedToken);
                    }
                    logger.info(String.format("User: %s decrypted password is : %s", username, encryptedPassword));


                }
                String successString = String.format("User: %s record is present in the Database. Check logs for your decrypted password.", username);
                logger.info(successString);

                return new ResponseDTO(true, successString, null);

            } else
                throw new Exception(String.format("User: %s record is not found in the Database.", username));

        } catch (Exception e) {

            String errMessage = String.format("Error in fetching the user record");
            logger.error(errMessage);
            e.printStackTrace();

            return new ResponseDTO(false, errMessage, e.getLocalizedMessage());
        }
    }


    /**
     * Method for retrieving the lookup table from S3 bucket
     * {@link S3Service}
     */
    private Optional<ResponseInputStream> fetchLookUpTableObjectFromS3Bucket() {
        return s3Service.getObject("csci-5410-monil-bucket-1", "Lookup5410.txt");
    }

    /**
     * Utility method for creating a HashMap from the stored Lockup Table.
     * The constructed map is based either the encryption or decryption process.
     * <p>
     * If the param - securityType = encrypt, the key of the map will be the alphabet of the lookup table
     * and value will the Replacement(sample encoding value)
     */
    private Map<String, String> getMapFromLookUpTable(ResponseInputStream responseInputStream, String securityType) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(responseInputStream));
        Map<String, String> map = new HashMap<>();

        int count = 0;
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                if (count != 0) {
                    if (!securityType.isEmpty() && securityType.equalsIgnoreCase("encrypt"))
                        map.put(String.valueOf(line.charAt(0)), line.substring(1).trim());
                    else if (!securityType.isEmpty() && securityType.equalsIgnoreCase("decrypt"))
                        map.put(line.substring(1).trim(), String.valueOf(line.charAt(0)));
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in parsing object");
            throw e;
        }
        logger.info(String.format("Map generated from LookUpTable: %s", map));
        return map;
    }
}