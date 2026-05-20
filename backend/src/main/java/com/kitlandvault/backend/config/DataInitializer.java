package com.kitlandvault.backend.config;

import com.kitlandvault.backend.entities.*;
import com.kitlandvault.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds initial data on startup if the database is empty.
 * Creates: 1 family, 1 user, 6 wallets, wallet goals, and categories.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final WalletGoalRepository walletGoalRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping initialization.");
            return;
        }

        log.info("Seeding database with initial data...");

        // 1. Create Family Group
        FamilyGroup family = FamilyGroup.builder()
                .name("Kitsana Family")
                .build();
        entityManager.persist(family);
        entityManager.flush();

        // 2. Create Users
        User admin = User.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("KitVault@2026"))
                .role("ADMIN")
                .familyGroup(family)
                .build();
        admin = userRepository.save(admin);
        log.info("Created admin user: {} (id={})", admin.getUsername(), admin.getId());

        User kit = User.builder()
                .username("kit")
                .passwordHash(passwordEncoder.encode("password123"))
                .role("USER")
                .familyGroup(family)
                .build();
        kit = userRepository.save(kit);
        log.info("Created user: {} (id={})", kit.getUsername(), kit.getId());

        // 3. Create 2 Template Wallets
        Wallet daily = createWallet(admin, "บัญชีประจำวัน", Wallet.AccountRole.DAILY,
                new BigDecimal("18000"), new BigDecimal("600"), new BigDecimal("3000"), null, "#6B8E7B");
        Wallet savings = createWallet(admin, "บัญชีเงินเก็บ", Wallet.AccountRole.SINKING_FUND,
                new BigDecimal("80000"), null, null, null, "#5D9C96");

        log.info("Created 2 template wallets: บัญชีประจำวัน, บัญชีเงินเก็บ");

        // 4. Create Wallet Goals for บัญชีเงินเก็บ
        createGoal(savings, "Emergency - Mother Surgery", new BigDecimal("30000"), new BigDecimal("15000"), 1);
        createGoal(savings, "Annual HOA & Insurance", new BigDecimal("12000"), new BigDecimal("8000"), 2);
        createGoal(savings, "Car Maintenance", new BigDecimal("10000"), new BigDecimal("5000"), 3);
        log.info("Created 3 wallet goals for บัญชีเงินเก็บ");

        // 5. Create Categories
        // Expense categories
        createCategory("Food & Dining", "ONE_TIME", "EXPENSE");
        createCategory("Groceries", "RECURRING", "EXPENSE");
        createCategory("Transport", "ONE_TIME", "EXPENSE");
        createCategory("Utilities", "RECURRING", "EXPENSE");
        createCategory("Internet", "RECURRING", "EXPENSE");
        createCategory("Mortgage", "RECURRING", "EXPENSE");
        createCategory("Car Installment", "RECURRING", "EXPENSE");
        createCategory("Insurance", "RECURRING", "EXPENSE");
        createCategory("Subscriptions", "RECURRING", "EXPENSE");
        createCategory("Parents Allowance", "RECURRING", "EXPENSE");
        createCategory("Entertainment", "ONE_TIME", "EXPENSE");
        createCategory("Shopping", "ONE_TIME", "EXPENSE");
        createCategory("Healthcare", "ONE_TIME", "EXPENSE");
        createCategory("Education", "ONE_TIME", "EXPENSE");
        createCategory("Other Expense", "ONE_TIME", "EXPENSE");

        // Income categories
        createCategory("Salary", "RECURRING", "INCOME");
        createCategory("Bonus", "ONE_TIME", "INCOME");
        createCategory("Freelance", "ONE_TIME", "INCOME");
        createCategory("Other Income", "ONE_TIME", "INCOME");

        log.info("Created {} categories", categoryRepository.count());
        log.info("Database seeding complete!");
    }

    private Wallet createWallet(User user, String name, Wallet.AccountRole role,
                                BigDecimal balance, BigDecimal dailyBudget,
                                BigDecimal reserveAmount, BigDecimal minBalance, String color) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .name(name)
                .accountRole(role)
                .color(color)
                .balance(balance)
                .dailyBudget(dailyBudget)
                .reserveAmount(reserveAmount != null ? reserveAmount : BigDecimal.ZERO)
                .minBalance(minBalance != null ? minBalance : BigDecimal.ZERO)
                .build();
        return walletRepository.save(wallet);
    }

    private void createGoal(Wallet wallet, String name, BigDecimal target, BigDecimal current, int priority) {
        WalletGoal goal = WalletGoal.builder()
                .wallet(wallet)
                .name(name)
                .targetAmount(target)
                .currentAmount(current)
                .priority(priority)
                .build();
        walletGoalRepository.save(goal);
    }

    private void createCategory(String name, String type, String transactionType) {
        Category category = Category.builder()
                .name(name)
                .type(Category.Type.valueOf(type))
                .transactionType(Category.TransactionType.valueOf(transactionType))
                .build();
        categoryRepository.save(category);
    }
}
