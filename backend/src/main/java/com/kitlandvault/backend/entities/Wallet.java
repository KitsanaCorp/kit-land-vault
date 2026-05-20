package com.kitlandvault.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    public enum AccountRole {
        TRANSIT,        // BBL — receives salary, must clear to 0
        DAILY,          // Kasikorn — daily spending, has daily allowance
        BILLS,          // LHB You — fixed costs, maintains min balance
        CAR_LOAN,       // SCB — strictly for car installments
        SINKING_FUND,   // Kept — high-interest reserve with sub-goals
        INVESTMENT,     // Dime — holding for stocks/index funds
        CUSTOM          // Custom user-defined wallet
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_role", nullable = false, length = 20)
    private AccountRole accountRole;

    @Column(name = "color", length = 7)
    private String color;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private java.util.List<WalletGoal> goals;

    @OneToMany(mappedBy = "wallet")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private java.util.List<Transaction> transactions;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(name = "daily_budget", precision = 12, scale = 2)
    private BigDecimal dailyBudget;

    @Column(name = "reserve_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal reserveAmount = BigDecimal.ZERO;

    @Column(name = "min_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minBalance = BigDecimal.ZERO;

    @Column(name = "budget_reset_day")
    @Builder.Default
    private Integer budgetResetDay = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreRemove
    private void preRemove() {
        if (transactions != null) {
            transactions.forEach(t -> t.setWallet(null));
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
