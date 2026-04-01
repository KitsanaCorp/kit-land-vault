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

        BigDecimal totalBudget = wallet.getDailyBudget().multiply(BigDecimal.valueOf(totalDays));
        BigDecimal remaining = wallet.getBalance();
        BigDecimal spent = totalBudget.subtract(remaining);
        BigDecimal dailyRate = daysRemaining > 0
                ? remaining.divide(BigDecimal.valueOf(daysRemaining), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return DailyBudgetResponse.builder()
                .walletId(walletId)
                .walletName(wallet.getName())
                .totalBudget(totalBudget)
                .spent(spent)
                .remaining(remaining)
                .dailyRate(dailyRate)
                .daysRemaining(daysRemaining)
                .build();
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .balance(w.getBalance())
                .dailyBudget(w.getDailyBudget())
                .budgetResetDay(w.getBudgetResetDay())
                .build();
    }
}
