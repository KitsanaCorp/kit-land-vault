package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBudgetResponse {
    /** Combined balance of all DAILY wallets */
    private BigDecimal totalBalance;

    /** Sum of transactions (expenses) made from DAILY wallets this month */
    private BigDecimal spentThisMonth;

    /** Effective balance remaining (totalBalance - spentThisMonth) */
    private BigDecimal remaining;

    /** How much can be spent per day for the rest of the month */
    private BigDecimal dailyRate;

    /** Days left in the current month (including today) */
    private int daysRemaining;

    /** Number of DAILY wallets contributing to this summary */
    private int walletCount;

    /** Names of the DAILY wallets included */
    private List<String> walletNames;
}
