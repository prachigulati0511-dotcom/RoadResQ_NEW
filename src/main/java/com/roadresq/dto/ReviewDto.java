package com.roadresq.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotNull(message = "Star rating is required")
    @Min(1)
    @Max(5)
    private Integer stars;

    private String tags; // e.g. "Prompt Arrival, Professional Mechanic, Clean Towing"
    private String comment;
    private String customerName;
    private String providerName;
    private LocalDateTime createdAt;

    public ReviewDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
