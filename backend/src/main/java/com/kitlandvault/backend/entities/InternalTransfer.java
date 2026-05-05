package com.kitlandvault.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks borrowing between the user's OWN wallets (inter-account),
 * as opposed to {@link Loan} which tracks borrowing between PEOPLE (inter-person).
 *
 * Example: Kasikorn (Daily) borrows ฿5,000 from Dime (Investment).
 */
@Entity
@Table(name = "internal_transfers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalTransfer {

    public enum Status {
        ACTIVE,
        PARTIALLY_REPAID,
        REPAID
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id", nullable = false)
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_wallet_id", nullable = false)
    private Wallet destinationWallet;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "remaining_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "repaid_at")
    private LocalDateTime repaidAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
