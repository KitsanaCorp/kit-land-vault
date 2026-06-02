package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.RepaymentRequest;
import com.kitlandvault.backend.dto.SettlementBalanceResponse;
import com.kitlandvault.backend.dto.SettlementResponse;
import com.kitlandvault.backend.entities.Settlement;
import com.kitlandvault.backend.entities.User;
import com.kitlandvault.backend.entities.Wallet;
import com.kitlandvault.backend.entities.Transaction;
import com.kitlandvault.backend.repositories.SettlementRepository;
import com.kitlandvault.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    /**
     * Auto-create a RECEIVABLE settlement when a shared/on-behalf transaction is recorded.
     */
    @Transactional
    public void createReceivable(User creditor, User debtor, Transaction transaction, BigDecimal partnerShare) {
        Settlement settlement = Settlement.builder()
                .creditor(creditor)
                .debtor(debtor)
                .transaction(transaction)
                .amount(partnerShare)
                .type(Settlement.Type.RECEIVABLE)
                .status(Settlement.Status.PENDING)
                .build();
        settlementRepository.save(settlement);
    }

    /**
     * Get the net balance: how much the debtor owes the creditor.
     */
    public SettlementBalanceResponse getBalance(Long creditorId, Long debtorId) {
        BigDecimal totalReceivable = settlementRepository
                .sumAmountByCreditorAndDebtorAndType(
                        creditorId, debtorId, Settlement.Type.RECEIVABLE);

        BigDecimal totalRepaid = settlementRepository
                .sumAmountByCreditorAndDebtorAndType(
                        creditorId, debtorId, Settlement.Type.REPAYMENT);

        BigDecimal net = totalReceivable.subtract(totalRepaid);

        return SettlementBalanceResponse.builder()
                .creditorId(creditorId)
                .debtorId(debtorId)
                .totalReceivable(totalReceivable)
                .totalRepaid(totalRepaid)
                .netBalance(net)
                .build();
    }

    /**
     * Record repayment: wallet replenishment only, NOT income.
     */
    @Transactional
    public void recordRepayment(Long creditorId, RepaymentRequest request) {
        User creditor = userRepository.findById(creditorId)
                .orElseThrow(() -> new RuntimeException("Creditor not found"));
        User debtor = userRepository.findById(request.getDebtorId())
                .orElseThrow(() -> new RuntimeException("Debtor not found"));
        Wallet targetWallet = walletService.getWalletEntity(request.getTargetWalletId());

        // 1. Create REPAYMENT settlement
        Settlement repayment = Settlement.builder()
                .creditor(creditor)
                .debtor(debtor)
                .amount(request.getAmount())
                .type(Settlement.Type.REPAYMENT)
                .status(Settlement.Status.SETTLED)
                .targetWallet(targetWallet)
                .settledAt(LocalDateTime.now())
                .note(request.getNote())
                .build();
        settlementRepository.save(repayment);

        // 2. Replenish target wallet (NOT an income transaction!)
        walletService.addBalance(request.getTargetWalletId(), request.getAmount());

        // 3. Mark matching RECEIVABLE rows as SETTLED (FIFO)
        settleReceivables(creditorId, request.getDebtorId(), request.getAmount());
    }

    private void settleReceivables(Long creditorId, Long debtorId, BigDecimal repaymentAmount) {
        List<Settlement> pendingReceivables = settlementRepository
                .findByCreditorIdAndDebtorIdAndTypeAndStatus(
                        creditorId, debtorId, Settlement.Type.RECEIVABLE, Settlement.Status.PENDING);

        BigDecimal remaining = repaymentAmount;
        List<Settlement> toSave = new java.util.ArrayList<>();

        for (Settlement receivable : pendingReceivables) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            if (remaining.compareTo(receivable.getAmount()) >= 0) {
                remaining = remaining.subtract(receivable.getAmount());
                receivable.setStatus(Settlement.Status.SETTLED);
                receivable.setSettledAt(LocalDateTime.now());
                toSave.add(receivable);
            } else {
                // Split the receivable
                BigDecimal settledPart = remaining;
                BigDecimal pendingPart = receivable.getAmount().subtract(settledPart);

                // Update current row to the settled portion
                receivable.setAmount(settledPart);
                receivable.setStatus(Settlement.Status.SETTLED);
                receivable.setSettledAt(LocalDateTime.now());
                toSave.add(receivable);

                // Create a new pending row for the leftover portion
                Settlement leftover = Settlement.builder()
                        .creditor(receivable.getCreditor())
                        .debtor(receivable.getDebtor())
                        .transaction(receivable.getTransaction())
                        .amount(pendingPart)
                        .type(Settlement.Type.RECEIVABLE)
                        .status(Settlement.Status.PENDING)
                        .build();
                leftover.setCreatedAt(receivable.getCreatedAt()); // Maintain original FIFO sorting
                toSave.add(leftover);

                remaining = BigDecimal.ZERO;
                break;
            }
        }
        settlementRepository.saveAll(toSave);
    }

    /**
     * Get full settlement history between two users.
     */
    public List<SettlementResponse> getHistory(Long creditorId, Long debtorId) {
        return settlementRepository
                .findByCreditorIdAndDebtorIdOrderByCreatedAtDesc(creditorId, debtorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SettlementResponse toResponse(Settlement s) {
        return SettlementResponse.builder()
                .id(s.getId())
                .type(s.getType().name())
                .status(s.getStatus().name())
                .amount(s.getAmount())
                .transactionDescription(s.getTransaction() != null ? s.getTransaction().getDescription() : null)
                .targetWalletName(s.getTargetWallet() != null ? s.getTargetWallet().getName() : null)
                .note(s.getNote())
                .createdAt(s.getCreatedAt())
                .settledAt(s.getSettledAt())
                .build();
    }
}
