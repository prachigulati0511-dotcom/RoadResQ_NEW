package com.roadresq.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class QuoteDto {

    @NotNull(message = "Revised quote amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal revisedQuote;

    private String reason;

    public QuoteDto() {}

    public QuoteDto(BigDecimal revisedQuote, String reason) {
        this.revisedQuote = revisedQuote;
        this.reason = reason;
    }

    public BigDecimal getRevisedQuote() { return revisedQuote; }
    public void setRevisedQuote(BigDecimal revisedQuote) { this.revisedQuote = revisedQuote; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
