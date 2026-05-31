package com.kitlandvault.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupSummaryResponse {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupBalanceDetail {
        private Long groupId;
        private String groupName;
        private BigDecimal netBalance;
    }

    private BigDecimal totalOwedToMe;
    private BigDecimal totalIOweToOthers;
    private BigDecimal overallNetBalance;
    private List<GroupBalanceDetail> groupBalances;
}
