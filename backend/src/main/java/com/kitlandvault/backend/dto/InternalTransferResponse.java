package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalTransferResponse {
    private Long id;
    private Long userId;
    private String sourceWalletName;
    private Long sourceWalletId;
    private String destinationWalletName;
    private Long destinationWalletId;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime repaidAt;
}
