package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementBalanceResponse {
    private Long creditorId;
    private Long debtorId;
    private BigDecimal totalReceivable;
    private BigDecimal totalRepaid;
    private BigDecimal netBalance;
}
