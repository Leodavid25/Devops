package com.devops.controller;

import com.devops.dto.DevOpsRequest;
import com.devops.dto.DevOpsResponse;
import com.devops.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DevOpsController {

    public static final String JWT_HEADER = "X-JWT-KWY";

    private final JwtService jwtService;

    public DevOpsController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/DevOps", produces = "application/json")
    public ResponseEntity<DevOpsResponse> handleDevOps(
            @RequestHeader(value = JWT_HEADER, required = false) String jwt,
            @Valid @RequestBody DevOpsRequest request) {

        jwtService.validateAndConsume(jwt);

        String greeting = "Hello " + request.getTo() + " your message will be send";
        return ResponseEntity.ok(new DevOpsResponse(greeting));
    }

    
    @RequestMapping(value = "/DevOps", method = {
            RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE,
            RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS
    }, produces = "text/plain")
    public ResponseEntity<String> rejectOtherMethods() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("ERROR");
    }

    @ExceptionHandler(JwtService.JwtValidationException.class)
    public ResponseEntity<String> handleJwtValidation(JwtService.JwtValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ERROR");
    }
}
