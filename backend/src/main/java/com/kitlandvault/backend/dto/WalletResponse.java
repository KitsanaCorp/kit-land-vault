package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {
    private Long id;
    private String name;
    private String accountRole;
    private BigDecimal balance;
    private BigDecimal dailyBudget;
    private BigDecimal reserveAmount;
    private BigDecimal minBalance;
    private Integer budgetResetDay;
}
