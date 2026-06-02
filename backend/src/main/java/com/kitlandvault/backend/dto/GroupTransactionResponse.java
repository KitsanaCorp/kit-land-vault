package com.kitlandvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTransactionResponse {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SplitDetail {
        private Long id;
        private Long userId;
        private String username;
        private BigDecimal shareAmount;
        private String status;
        private LocalDateTime settledAt;
    }

    private Long id;
    private Long groupId;
    private String groupName;
    private Long payerId;
    private String payerName;
    private BigDecimal amount;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long walletId;
    private String walletName;
    private LocalDate transactionDate;
    private List<SplitDetail> splits;
    private String createdBy;
    private LocalDateTime createdAt;
}
