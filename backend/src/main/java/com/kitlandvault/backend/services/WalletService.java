package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.DailyBudgetResponse;
import com.kitlandvault.backend.dto.WalletRequest;
import com.kitlandvault.backend.dto.WalletResponse;
import com.kitlandvault.backend.entities.User;
import com.kitlandvault.backend.entities.Wallet;
import com.kitlandvault.backend.repositories.UserRepository;
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
    private final UserRepository userRepository;

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

    @Transactional
    public WalletResponse createWallet(Long userId, WalletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Wallet.AccountRole role = Wallet.AccountRole.CUSTOM;
        if (request.getAccountRole() != null) {
            try {
                role = Wallet.AccountRole.valueOf(request.getAccountRole());
            } catch (IllegalArgumentException e) {
                // Default to CUSTOM
            }
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .name(request.getName())
                .accountRole(role)
                .color(request.getColor())
                .balance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO)
                .dailyBudget(request.getDailyBudget())
                .reserveAmount(request.getReserveAmount() != null ? request.getReserveAmount() : BigDecimal.ZERO)
                .minBalance(request.getMinBalance() != null ? request.getMinBalance() : BigDecimal.ZERO)
                .budgetResetDay(request.getBudgetResetDay() != null ? request.getBudgetResetDay() : 1)
                .build();

        return toResponse(walletRepository.save(wallet));
    }

    @Transactional
    public WalletResponse updateWallet(Long walletId, WalletRequest request) {
        Wallet wallet = getWalletEntity(walletId);

        if (request.getName() != null) {
            wallet.setName(request.getName());
        }
        if (request.getAccountRole() != null) {
            try {
                wallet.setAccountRole(Wallet.AccountRole.valueOf(request.getAccountRole()));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        if (request.getColor() != null) {
            wallet.setColor(request.getColor());
        }
        if (request.getBalance() != null) {
            wallet.setBalance(request.getBalance());
        }
        if (request.getDailyBudget() != null) {
            wallet.setDailyBudget(request.getDailyBudget());
        }
        if (request.getReserveAmount() != null) {
            wallet.setReserveAmount(request.getReserveAmount());
        }
        if (request.getMinBalance() != null) {
            wallet.setMinBalance(request.getMinBalance());
        }
        if (request.getBudgetResetDay() != null) {
            wallet.setBudgetResetDay(request.getBudgetResetDay());
        }

        return toResponse(walletRepository.save(wallet));
    }

    @Transactional
    public void deleteWallet(Long walletId) {
        Wallet wallet = getWalletEntity(walletId);
        walletRepository.delete(wallet);
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .accountRole(w.getAccountRole() != null ? w.getAccountRole().name() : null)
                .color(w.getColor())
                .balance(w.getBalance())
                .dailyBudget(w.getDailyBudget())
                .reserveAmount(w.getReserveAmount())
                .minBalance(w.getMinBalance())
                .budgetResetDay(w.getBudgetResetDay())
                .build();
    }
}
