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
public class WalletRequest {
    private String name;
    private String accountRole;
    private String color;
    private BigDecimal balance;
    private BigDecimal dailyBudget;
    private BigDecimal reserveAmount;
    private BigDecimal minBalance;
    private Integer budgetResetDay;
}
