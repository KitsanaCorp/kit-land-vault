package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletGoalResponse {
    private Long id;
    private Long walletId;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Integer priority;
    private BigDecimal progressPercent;
}
