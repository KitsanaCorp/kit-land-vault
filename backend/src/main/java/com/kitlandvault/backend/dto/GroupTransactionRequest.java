package com.kitlandvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTransactionRequest {
    private Long payerId;
    private BigDecimal amount;
    private String description;
    private Long categoryId;
    private Long walletId;
    private String transactionDate;
}
