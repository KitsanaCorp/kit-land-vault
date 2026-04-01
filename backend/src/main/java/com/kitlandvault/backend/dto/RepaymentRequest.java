package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentRequest {
    private Long debtorId;
    private BigDecimal amount;
    private Long targetWalletId;
    private String note;
}
