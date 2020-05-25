# Spring boot Java application for accessing AWS S3 and RDS services using the official AWS SDK for Java - Version 2

This is a Java application built using Spring boot for accessing AWS S3 and RDS services using the AWS Java SDK.


There are two parts to this project.

1) AWS S3 bucket

    This module exposes RESTful API endpoints for creating S3 bucket, uploading object (local file) to the bucket, and copying 
    object from source to destination bucket.
    
2) AWS RDS

    This module exposes RESTful API endpoints for inserting and retrieving user record (username, password) from AWS RDS - MySQL DB.
    


 
## Installation

Import this project in Intellij/eclipse.

Using [gradle](https://gradle.org/install/), build the project by running following command:

```bash
gradle clean install
```

## Usage

This is AWS S3 and RDS services dependent project, which connects to an existing AWS RDS instance.

1. Create AWS RDS instance from AWS services dashboard and add the connection details in the `application.properties` file.
2. Update the driver details in    `com.aws.services.util.connection.JDBCConnectionBuilder.java` class if the database instance is other than MySQL.
3. Add the `AWS key Id, key secret and session token` in `application.properties` file.


#### Run the project
```
Run the main() of the com.aws.services.Assignment1Application.java
```

This will spawn an embedded web server running on port: http://localhost:8080/.

#### API endpoints

##### 1) S3 functionalities

- Create new S3 bucket


> URI: http://localhost:8080/s3/bucket
>
> method: POST
>
> body: {
            "s3BucketName": "csci-5410-new-bucket-restrictive"
        }


- Upload object to S3 bucket 

> URI: http://localhost:8080/s3/file/upload/
>
> method: POST
>
> body: {
            "directory": "/file-directory",
            "file": "filename",
            "s3BucketName": "S3 bucket name",
            "key": "filename-key"
        }

- Copy object from source to destination bucket

> URI: http://localhost:8080/s3/object/copy/
>
> method: POST
>
> body: {
            "sourceBucket": "bucket-1",
            "destinationBucket": "bucket-2",
            "key": "bucket-1 object-key"
        }


##### 2) RDS API
- Create user

> URI: http://localhost:8080/rds/user
>
> method: POST
>
>body: {
           "username": "user-1",
           "password": "password"
       }

This endpoint uses a simple key-value encoding mechanism for storing the user credentials in encrypted format in the RDS database table. 
A lookup table (text file located under the resource directory) contains a key value pair, needs to be upload to S3 bucket, for the first time.

This file will be downloaded during the user record insertion and a lookup will be made to encode the password.
E.g. if password contains “a”, it replaces a with “xt”. For every character, it encodes the value based on this lookup table. 

The encrypted password and username is stored in RDS

- Retrieve user

> URI: http://localhost:8080/rds/user?username='user'
>
> method: GET


This endpoing fetches the user record from RDS. 
It downloads the lookup table from S3 bucket, decrypts the password and prints the original password in the console


## Library reference
[AWS-SDK](https://github.com/aws/aws-sdk-java-v2)
