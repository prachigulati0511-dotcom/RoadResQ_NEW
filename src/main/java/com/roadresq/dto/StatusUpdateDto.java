package com.roadresq.dto;

import com.roadresq.model.RequestStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateDto {

    @NotNull(message = "Status is required")
    private RequestStatus status;

    private String cancelReason;

    public StatusUpdateDto() {}

    public StatusUpdateDto(RequestStatus status, String cancelReason) {
        this.status = status;
        this.cancelReason = cancelReason;
    }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
}
