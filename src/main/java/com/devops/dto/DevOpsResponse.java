package com.devops.dto;


public class DevOpsResponse {

    private String message;

    public DevOpsResponse() {
    }

    public DevOpsResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
