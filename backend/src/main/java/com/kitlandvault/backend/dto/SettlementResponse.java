package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponse {
    private Long id;
    private String type;     // RECEIVABLE or REPAYMENT
    private String status;   // PENDING or SETTLED
    private BigDecimal amount;
    private String transactionDescription;
    private String targetWalletName;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime settledAt;
}
