package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBudgetResponse {
    private Long walletId;
    private String walletName;
    private BigDecimal totalBudget;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal dailyRate;
    private int daysRemaining;
}
