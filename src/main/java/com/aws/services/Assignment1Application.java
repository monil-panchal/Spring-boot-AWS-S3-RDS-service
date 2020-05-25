package com.aws.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/*
 * Spring boot starter class, serving as an entry point to this application.
 * Simply run the main method which will spawn an embedded server listening on http://localhost:9090/
 * Configurations are defined in application.properties file in resource directory.
 */

@SpringBootApplication
public class Assignment1Application {

    public static void main(String[] args) {
        SpringApplication.run(Assignment1Application.class, args);
        System.out.println("AWS S3 and RDS application has started...");
    }
}
