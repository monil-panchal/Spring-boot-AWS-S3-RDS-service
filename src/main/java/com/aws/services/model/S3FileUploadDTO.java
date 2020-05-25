package com.aws.services.model;

import com.aws.services.controller.S3Contoller;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Request body bean for uploading object to S3 bucket.
 *
 * @see S3Contoller
 */
@Component
@Data
@Getter
public class S3FileUploadDTO {

    @NotNull
    private String directory;

    @NotNull
    private String file;

    @NotNull
    private String s3BucketName;

    @NotNull
    private String key;

}