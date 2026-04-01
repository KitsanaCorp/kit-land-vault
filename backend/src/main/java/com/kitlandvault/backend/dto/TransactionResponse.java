package com.kitlandvault.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private Long userId;
    private String walletName;
    private Long walletId;
    private String categoryName;
    private BigDecimal amount;
    private String splitType;
    private BigDecimal myShare;
    private BigDecimal partnerShare;
    private LocalDate transactionDate;
    private String description;
}
