package com.torin.prod.dto;

public class OperationStatusDto {
    private Boolean status;
    private String message;

    public OperationStatusDto() {
    }
    
    public OperationStatusDto(Boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(boolean stat) {
        this.status = stat;
    }

    public void setMessage(String mess) {
        this.message = mess;
    }
}
