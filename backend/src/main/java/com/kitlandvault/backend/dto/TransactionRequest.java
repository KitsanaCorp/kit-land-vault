package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    private Long categoryId;
    private Long walletId;
    private BigDecimal amount;
    private String splitType;  // PERSONAL, SHARED, ON_BEHALF
    private String transactionDate; // yyyy-MM-dd
    private String description;
}
