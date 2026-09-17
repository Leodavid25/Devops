package com.devops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class DevOpsRequest {

    @NotBlank(message = "message must not be blank")
    private String message;

    @NotBlank(message = "to must not be blank")
    private String to;

    @NotBlank(message = "from must not be blank")
    @JsonProperty("from")
    private String from;

    @NotNull(message = "timeToLifeSec must not be null")
    @Positive(message = "timeToLifeSec must be positive")
    private Integer timeToLifeSec;

    public DevOpsRequest() {
    }

    public DevOpsRequest(String message, String to, String from, Integer timeToLifeSec) {
        this.message = message;
        this.to = to;
        this.from = from;
        this.timeToLifeSec = timeToLifeSec;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Integer getTimeToLifeSec() {
        return timeToLifeSec;
    }

    public void setTimeToLifeSec(Integer timeToLifeSec) {
        this.timeToLifeSec = timeToLifeSec;
    }
}
