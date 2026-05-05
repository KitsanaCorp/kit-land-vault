package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.DailyBudgetResponse;
import com.kitlandvault.backend.dto.WalletResponse;
import com.kitlandvault.backend.entities.Wallet;
import com.kitlandvault.backend.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public List<WalletResponse> getWalletsByUser(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Wallet getWalletEntity(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));
    }

    @Transactional
    public void deductBalance(Long walletId, BigDecimal amount) {
        Wallet wallet = getWalletEntity(walletId);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void addBalance(Long walletId, BigDecimal amount) {
        Wallet wallet = getWalletEntity(walletId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    public DailyBudgetResponse getDailyBudget(Long walletId) {
        Wallet wallet = getWalletEntity(walletId);

        if (wallet.getDailyBudget() == null) {
            throw new RuntimeException("Wallet does not have a daily budget configured");
        }

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        int totalDays = currentMonth.lengthOfMonth();
        int dayOfMonth = today.getDayOfMonth();
        int daysRemaining = totalDays - dayOfMonth + 1; // including today

        // Deduct reserve (e.g., ฿3,000 for groceries) before calculating daily allowance
        BigDecimal reserve = wallet.getReserveAmount() != null
                ? wallet.getReserveAmount() : BigDecimal.ZERO;
        BigDecimal spendableBalance = wallet.getBalance().subtract(reserve);
        if (spendableBalance.compareTo(BigDecimal.ZERO) < 0) {
            spendableBalance = BigDecimal.ZERO;
        }

        BigDecimal totalBudget = wallet.getDailyBudget().multiply(BigDecimal.valueOf(totalDays));
        BigDecimal spent = totalBudget.subtract(spendableBalance);
        BigDecimal dailyRate = daysRemaining > 0
                ? spendableBalance.divide(BigDecimal.valueOf(daysRemaining), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return DailyBudgetResponse.builder()
                .walletId(walletId)
                .walletName(wallet.getName())
                .accountRole(wallet.getAccountRole() != null ? wallet.getAccountRole().name() : null)
                .totalBudget(totalBudget)
                .reserveAmount(reserve)
                .spent(spent)
                .remaining(spendableBalance)
                .dailyRate(dailyRate)
                .daysRemaining(daysRemaining)
                .build();
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .accountRole(w.getAccountRole() != null ? w.getAccountRole().name() : null)
                .balance(w.getBalance())
                .dailyBudget(w.getDailyBudget())
                .reserveAmount(w.getReserveAmount())
                .minBalance(w.getMinBalance())
                .budgetResetDay(w.getBudgetResetDay())
                .build();
    }
}
