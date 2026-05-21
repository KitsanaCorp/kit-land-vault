package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.DailyBudgetResponse;
import com.kitlandvault.backend.dto.WalletRequest;
import com.kitlandvault.backend.dto.WalletResponse;
import com.kitlandvault.backend.entities.Transaction;
import com.kitlandvault.backend.entities.User;
import com.kitlandvault.backend.entities.Wallet;
import com.kitlandvault.backend.repositories.TransactionRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

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

    /**
     * Calculates a daily budget by aggregating ALL wallets with role=DAILY for the given user.
     * dailyRate = (totalBalance - spentThisMonth) / daysRemainingInMonth
     * No stored dailyBudget field is required — it is derived purely from balances and transactions.
     */
    public DailyBudgetResponse getDailySummary(Long userId) {
        // 1. Get all DAILY wallets for this user
        List<Wallet> allWallets = walletRepository.findByUserId(userId);
        List<Wallet> dailyWallets = allWallets.stream()
                .filter(w -> Wallet.AccountRole.DAILY.equals(w.getAccountRole()))
                .collect(Collectors.toList());

        if (dailyWallets.isEmpty()) {
            return DailyBudgetResponse.builder()
                    .totalBalance(BigDecimal.ZERO)
                    .spentThisMonth(BigDecimal.ZERO)
                    .remaining(BigDecimal.ZERO)
                    .dailyRate(BigDecimal.ZERO)
                    .daysRemaining(0)
                    .walletCount(0)
                    .walletNames(List.of())
                    .build();
        }

        // 2. Sum balances of all DAILY wallets
        BigDecimal totalBalance = dailyWallets.stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Sum this month's transactions from all DAILY wallets
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int daysRemaining = currentMonth.lengthOfMonth() - today.getDayOfMonth() + 1;

        BigDecimal spentThisMonth = BigDecimal.ZERO;
        for (Wallet w : dailyWallets) {
            List<Transaction> txs = transactionRepository
                    .findByWalletIdAndTransactionDateBetween(w.getId(), firstDayOfMonth, today);
            BigDecimal walletSpent = txs.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            spentThisMonth = spentThisMonth.add(walletSpent);
        }

        // 4. Remaining = totalBalance (since balance is already reduced by transactions)
        BigDecimal remaining = totalBalance;
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        // 5. dailyRate = remaining / daysRemaining
        BigDecimal dailyRate = daysRemaining > 0
                ? remaining.divide(BigDecimal.valueOf(daysRemaining), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<String> walletNames = dailyWallets.stream()
                .map(Wallet::getName)
                .collect(Collectors.toList());

        return DailyBudgetResponse.builder()
                .totalBalance(totalBalance)
                .spentThisMonth(spentThisMonth)
                .remaining(remaining)
                .dailyRate(dailyRate)
                .daysRemaining(daysRemaining)
                .walletCount(dailyWallets.size())
                .walletNames(walletNames)
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
