package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalTransferRequest {
    private Long sourceWalletId;
    private Long destinationWalletId;
    private BigDecimal amount;
    private String note;
}
