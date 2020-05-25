package com.aws.services.controller;


import com.aws.services.model.ResponseDTO;
import com.aws.services.model.S3BucketDTO;
import com.aws.services.model.S3FileUploadDTO;
import com.aws.services.model.S3CopyObject;
import com.aws.services.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/* AWS S3 API endpoint controller for this application.
 * URI : http://localhost:8080/s3
 */
@RestController
@RequestMapping("/s3")
public class S3Contoller {

    Logger logger = LoggerFactory.getLogger(S3Contoller.class);

    @Autowired
    S3Service s3Service;

    /*   API endpoint method for uploading an object to S3 bucket
     *   URI: ../s3
     *   body: {"directory":"../directory","file":"fileName","s3BucketName":"S3 bucket name","key":"new object key"}
     *   Method: POST
     */
    @PostMapping("/file/upload")
    public ResponseEntity<ResponseDTO> uploadFileToS3Bucket(@Valid @RequestBody S3FileUploadDTO body) {

        logger.info("Calling API for uploading file in S3 bucket.");
        HttpStatus httpStatus = null;

        ResponseDTO responseDTO = s3Service.uploadFileToS3Bucket(body.getDirectory(), body.getFile(), body.getS3BucketName(), body.getKey());

        if (responseDTO.getSuccess()) {
            httpStatus = HttpStatus.OK;
        } else {
            httpStatus = HttpStatus.CONFLICT;
        }
        return new ResponseEntity(responseDTO, httpStatus);
    }

    /*   API endpoint method for creating a new S3 bucket
     *   URI: ../bucket
     *   body: {"s3BucketName": "new-bucket-name"}
     *   Method: POST
     */
    @PostMapping("/bucket")
    public ResponseEntity<ResponseDTO> createS3Bucket(@Valid @RequestBody S3BucketDTO body) {

        logger.info("Calling API for creating new S3 bucket.");
        HttpStatus httpStatus = null;

        ResponseDTO responseDTO = s3Service.createS3Bucket(body.getS3BucketName());

        if (responseDTO.getSuccess()) {
            httpStatus = HttpStatus.OK;
        } else
            httpStatus = HttpStatus.CONFLICT;

        return new ResponseEntity(responseDTO, httpStatus);
    }

    /*   API endpoint method for copying the object from source to destination bucket
     *   URI: ../object/copy
     *   body: {"sourceBucket":"source-bucket","destinationBucket":"destination-bucket","key":"object key"}
     *   Method: POST
     */
    @PostMapping("/object/copy")
    public ResponseEntity<ResponseDTO> copyObject(@Valid @RequestBody S3CopyObject body) {

        logger.info("Calling API for copying object from source to destination S3 bucket.");
        HttpStatus httpStatus = null;

        ResponseDTO responseDTO = s3Service.copyObject(body.getSourceBucket(), body.getDestinationBucket(), body.getKey());

        if (responseDTO.getSuccess()) {
            httpStatus = HttpStatus.OK;
        } else
            httpStatus = HttpStatus.CONFLICT;

        return new ResponseEntity(responseDTO, httpStatus);
    }
}