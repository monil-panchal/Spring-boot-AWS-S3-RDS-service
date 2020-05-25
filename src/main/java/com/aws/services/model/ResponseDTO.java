package com.aws.services.model;

import lombok.*;
import org.springframework.stereotype.Component;

/**
 * Response body bean
 */
@Component
@Data
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {

    private Boolean success;
    private String message;
    private String error;
}
