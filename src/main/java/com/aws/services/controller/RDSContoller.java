package com.aws.services.controller;

import com.aws.services.model.ResponseDTO;
import com.aws.services.service.RDSService;
import com.aws.services.model.RDSUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/* AWS RDS API endpoint controller for this application.
 * URI : http://localhost:8080/rds
 */
@RestController
@RequestMapping("/rds")
public class RDSContoller {

    Logger logger = LoggerFactory.getLogger(RDSContoller.class);

    @Autowired
    RDSService rdsService;

    /*   API endpoint method for creating a user in AWS RDS DB
     *   URI: ../user
     *   body: {"username": "username", "password": "password" }
     *   Method: POST
     */
    @PostMapping("/user")
    public ResponseEntity<ResponseDTO> createUser(@Valid @RequestBody RDSUserDTO body) throws Exception {

        logger.info("Calling API for user creation.");

        HttpStatus httpStatus = null;
        ResponseDTO responseDTO = rdsService.insertUserRecord(body.getUsername(), body.getPassword());

        if (responseDTO.getSuccess()) {
            httpStatus = HttpStatus.OK;
        } else {
            httpStatus = HttpStatus.CONFLICT;
        }
        return new ResponseEntity(responseDTO, httpStatus);
    }

    /*   API endpoint method for fetching the user from AWS RDS DB
     *   URI: ../user
     *   query param: username
     *   Method: GET
     */
    @GetMapping("/user")
    public ResponseEntity<ResponseDTO> getUser(@NotNull @RequestParam("username") String username) throws Exception {

        logger.info("Calling API for user retrieval from RDS.");

        HttpStatus httpStatus = null;
        ResponseDTO responseDTO = rdsService.getUserRecord(username);
        if (responseDTO.getSuccess()) {
            httpStatus = HttpStatus.OK;
        } else {
            httpStatus = HttpStatus.CONFLICT;
        }
        return new ResponseEntity(responseDTO, httpStatus);
    }
}