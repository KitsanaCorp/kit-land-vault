package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.TransactionRequest;
import com.kitlandvault.backend.dto.TransactionResponse;
import com.kitlandvault.backend.entities.*;
import com.kitlandvault.backend.repositories.CategoryRepository;
import com.kitlandvault.backend.repositories.TransactionRepository;
import com.kitlandvault.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final WalletService walletService;
    private final SettlementService settlementService;

    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Wallet wallet = walletService.getWalletEntity(request.getWalletId());

        Transaction.SplitType splitType = Transaction.SplitType.valueOf(
                request.getSplitType() != null ? request.getSplitType() : "PERSONAL");

        BigDecimal amount = request.getAmount();
        BigDecimal myShare;
        BigDecimal partnerShare;

        // Calculate split
        switch (splitType) {
            case SHARED:
                myShare = amount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                partnerShare = amount.subtract(myShare);
                break;
            case ON_BEHALF:
                myShare = BigDecimal.ZERO;
                partnerShare = amount;
                break;
            default: // PERSONAL
                myShare = amount;
                partnerShare = BigDecimal.ZERO;
                break;
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .familyGroup(user.getFamilyGroup())
                .category(category)
                .wallet(wallet)
                .amount(amount)
                .splitType(splitType)
                .myShare(myShare)
                .partnerShare(partnerShare)
                .transactionDate(LocalDate.parse(request.getTransactionDate()))
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);

        // Deduct full amount from wallet (you physically paid it)
        walletService.deductBalance(wallet.getId(), amount);

        // If partner share exists, auto-create receivable
        if (partnerShare.compareTo(BigDecimal.ZERO) > 0) {
            // Get partner (wife) — for simplicity, get another user in same family
            User partner = getPartnerInFamily(user);
            if (partner != null) {
                settlementService.createReceivable(user, partner, transaction, partnerShare);
            }
        }

        return toResponse(transaction);
    }

    public List<TransactionResponse> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TransactionResponse> getTransactionsByDateRange(Long userId, LocalDate start, LocalDate end) {
        return transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Find the other user in the same family group (partner/wife).
     */
    private User getPartnerInFamily(User user) {
        if (user.getFamilyGroup() == null) return null;
        return userRepository.findByFamilyGroupId(user.getFamilyGroup().getId())
                .stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
    }

    private TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .userId(tx.getUser().getId())
                .walletName(tx.getWallet() != null ? tx.getWallet().getName() : null)
                .walletId(tx.getWallet() != null ? tx.getWallet().getId() : null)
                .categoryName(tx.getCategory().getName())
                .amount(tx.getAmount())
                .splitType(tx.getSplitType().name())
                .myShare(tx.getMyShare())
                .partnerShare(tx.getPartnerShare())
                .transactionDate(tx.getTransactionDate())
                .description(tx.getDescription())
                .build();
    }
}
