package com.aws.services.model;

import com.aws.services.controller.S3Contoller;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Request body bean for creating new bucket API for this application.
 *
 * @see S3Contoller
 */
@Component
@Data
@Getter
public class RDSUserDTO {

    @NotNull
    private String username;

    @NotNull
    @Pattern(regexp = "\\p{javaLowerCase}+")
    private String password;
}
