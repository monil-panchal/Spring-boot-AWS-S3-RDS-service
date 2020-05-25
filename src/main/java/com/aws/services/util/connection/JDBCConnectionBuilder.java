package com.aws.services.util.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Utility class for creating a singleton object of JDBC connection to AWS RDS.
 */
@Component
public class JDBCConnectionBuilder {

    private Logger logger = LoggerFactory.getLogger(JDBCConnectionBuilder.class);

    @Value("${rds.host}")
    private String host;

    @Value("${rds.port}")
    private String port;

    @Value("${rds.database}")
    private String database;

    @Value("${rds.username}")
    private String username;

    @Value("${rds.password}")
    private String password;

    /**
     * Method for creating JDBC connection to AWS RDS.
     */
    public Optional<Connection> createDBConnection() throws SQLException, ClassNotFoundException {

        Optional<Connection> connection = Optional.empty();
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = Optional.of(DriverManager.getConnection(url, username, password));

            logger.info("JDBC connection for AWS RDS created successfully");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in creating DB connection");
            throw e;
        }
        return connection;
    }
}
