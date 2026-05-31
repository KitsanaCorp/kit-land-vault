package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.RepaymentRequest;
import com.kitlandvault.backend.dto.SettlementBalanceResponse;
import com.kitlandvault.backend.entities.Settlement;
import com.kitlandvault.backend.entities.User;
import com.kitlandvault.backend.entities.Wallet;
import com.kitlandvault.backend.repositories.SettlementRepository;
import com.kitlandvault.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private SettlementService settlementService;

    private User creditor;
    private User debtor;
    private Wallet targetWallet;

    @BeforeEach
    void setUp() {
        creditor = User.builder().id(1L).username("admin").build();
        debtor = User.builder().id(2L).username("kit").build();
        targetWallet = Wallet.builder().id(10L).name("K Mobile").balance(BigDecimal.ZERO).build();
    }

    @Test
    void testGetBalance_CorrectSumSubtraction() {
        // Arrange
        when(settlementRepository.sumAmountByCreditorAndDebtorAndType(1L, 2L, Settlement.Type.RECEIVABLE))
                .thenReturn(new BigDecimal("2000.00"));
        when(settlementRepository.sumAmountByCreditorAndDebtorAndType(1L, 2L, Settlement.Type.REPAYMENT))
                .thenReturn(new BigDecimal("1200.00"));

        // Act
        SettlementBalanceResponse response = settlementService.getBalance(1L, 2L);

        // Assert
        assertEquals(new BigDecimal("2000.00"), response.getTotalReceivable());
        assertEquals(new BigDecimal("1200.00"), response.getTotalRepaid());
        assertEquals(new BigDecimal("800.00"), response.getNetBalance());
    }

    @Test
    void testRecordRepayment_PartialSplittingLogic() {
        // Arrange
        RepaymentRequest request = new RepaymentRequest();
        request.setDebtorId(2L);
        request.setAmount(new BigDecimal("400.00"));
        request.setTargetWalletId(10L);
        request.setNote("Partial settlement");

        when(userRepository.findById(1L)).thenReturn(Optional.of(creditor));
        when(userRepository.findById(2L)).thenReturn(Optional.of(debtor));
        when(walletService.getWalletEntity(10L)).thenReturn(targetWallet);

        List<Settlement> pendingReceivables = new ArrayList<>();
        pendingReceivables.add(Settlement.builder()
                .id(101L)
                .creditor(creditor)
                .debtor(debtor)
                .amount(new BigDecimal("1000.00"))
                .type(Settlement.Type.RECEIVABLE)
                .status(Settlement.Status.PENDING)
                .build());

        when(settlementRepository.findByCreditorIdAndDebtorIdAndTypeAndStatus(
                1L, 2L, Settlement.Type.RECEIVABLE, Settlement.Status.PENDING))
                .thenReturn(pendingReceivables);

        // Act
        settlementService.recordRepayment(1L, request);

        // Assert
        // Verify wallet replenishment
        verify(walletService, times(1)).addBalance(10L, new BigDecimal("400.00"));

        // Captures what got saved
        ArgumentCaptor<List<Settlement>> savedReceivablesCaptor = ArgumentCaptor.forClass(List.class);
        verify(settlementRepository, times(1)).saveAll(savedReceivablesCaptor.capture());

        List<Settlement> savedList = savedReceivablesCaptor.getValue();
        // Since it's split, we should have 2 settlements saved:
        // 1. The original row updated to 400.00 (SETTLED)
        // 2. A new pending row with 600.00 (PENDING)
        assertEquals(2, savedList.size());

        Settlement settledPart = savedList.get(0);
        assertEquals(new BigDecimal("400.00"), settledPart.getAmount());
        assertEquals(Settlement.Status.SETTLED, settledPart.getStatus());

        Settlement pendingPart = savedList.get(1);
        assertEquals(new BigDecimal("600.00"), pendingPart.getAmount());
        assertEquals(Settlement.Status.PENDING, pendingPart.getStatus());
    }
}
