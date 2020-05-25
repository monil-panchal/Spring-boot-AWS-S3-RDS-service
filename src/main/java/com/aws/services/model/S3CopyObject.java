package com.aws.services.model;

import com.aws.services.controller.S3Contoller;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Request body bean for copying object from source to destination bucket.
 *
 * @see S3Contoller
 */
@Component
@Data
@Getter
@ToString
public class S3CopyObject {

    @NotNull
    private String sourceBucket;

    @NotNull
    private String destinationBucket;

    @NotNull
    private String key;

}