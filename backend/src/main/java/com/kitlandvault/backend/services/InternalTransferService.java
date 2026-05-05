package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.InternalTransferRequest;
import com.kitlandvault.backend.dto.InternalTransferResponse;
import com.kitlandvault.backend.entities.InternalTransfer;
import com.kitlandvault.backend.entities.User;
import com.kitlandvault.backend.entities.Wallet;
import com.kitlandvault.backend.repositories.InternalTransferRepository;
import com.kitlandvault.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages inter-account borrowing (e.g., Daily wallet borrows from Investment wallet).
 * This is separate from SettlementService which handles inter-person debts.
 */
@Service
@RequiredArgsConstructor
public class InternalTransferService {

    private final InternalTransferRepository transferRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    /**
     * Create an inter-account transfer: deduct from source, add to destination.
     * Creates a tracking record for future repayment.
     */
    @Transactional
    public InternalTransferResponse createTransfer(Long userId, InternalTransferRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet source = walletService.getWalletEntity(request.getSourceWalletId());
        Wallet dest = walletService.getWalletEntity(request.getDestinationWalletId());

        if (source.getId().equals(dest.getId())) {
            throw new IllegalArgumentException("Source and destination wallets must be different");
        }

        BigDecimal amount = request.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        // Move money between wallets
        walletService.deductBalance(source.getId(), amount);
        walletService.addBalance(dest.getId(), amount);

        // Create tracking record
        InternalTransfer transfer = InternalTransfer.builder()
                .user(user)
                .sourceWallet(source)
                .destinationWallet(dest)
                .amount(amount)
                .remainingAmount(amount)
                .status(InternalTransfer.Status.ACTIVE)
                .note(request.getNote())
                .build();

        transfer = transferRepository.save(transfer);
        return toResponse(transfer);
    }

    /**
     * Repay (partially or fully) an inter-account transfer.
     * Moves money back from destination to source wallet.
     */
    @Transactional
    public InternalTransferResponse repayTransfer(Long transferId, BigDecimal repaymentAmount) {
        InternalTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));

        if (transfer.getStatus() == InternalTransfer.Status.REPAID) {
            throw new IllegalStateException("Transfer is already fully repaid");
        }

        if (repaymentAmount.compareTo(transfer.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Repayment amount exceeds remaining balance");
        }

        // Move money back: destination → source
        walletService.deductBalance(transfer.getDestinationWallet().getId(), repaymentAmount);
        walletService.addBalance(transfer.getSourceWallet().getId(), repaymentAmount);

        // Update tracking record
        BigDecimal newRemaining = transfer.getRemainingAmount().subtract(repaymentAmount);
        transfer.setRemainingAmount(newRemaining);

        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            transfer.setStatus(InternalTransfer.Status.REPAID);
            transfer.setRepaidAt(LocalDateTime.now());
        } else {
            transfer.setStatus(InternalTransfer.Status.PARTIALLY_REPAID);
        }

        transfer = transferRepository.save(transfer);
        return toResponse(transfer);
    }

    /**
     * Get all active (non-repaid) inter-account transfers for a user.
     */
    public List<InternalTransferResponse> getActiveTransfers(Long userId) {
        return transferRepository
                .findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, InternalTransfer.Status.REPAID)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get full transfer history for a user.
     */
    public List<InternalTransferResponse> getAllTransfers(Long userId) {
        return transferRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private InternalTransferResponse toResponse(InternalTransfer t) {
        return InternalTransferResponse.builder()
                .id(t.getId())
                .userId(t.getUser().getId())
                .sourceWalletName(t.getSourceWallet().getName())
                .sourceWalletId(t.getSourceWallet().getId())
                .destinationWalletName(t.getDestinationWallet().getName())
                .destinationWalletId(t.getDestinationWallet().getId())
                .amount(t.getAmount())
                .remainingAmount(t.getRemainingAmount())
                .status(t.getStatus().name())
                .note(t.getNote())
                .createdAt(t.getCreatedAt())
                .repaidAt(t.getRepaidAt())
                .build();
    }
}
