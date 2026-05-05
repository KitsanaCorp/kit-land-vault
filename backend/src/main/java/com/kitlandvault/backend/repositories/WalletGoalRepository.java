package com.kitlandvault.backend.repositories;

import com.kitlandvault.backend.entities.WalletGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletGoalRepository extends JpaRepository<WalletGoal, Long> {

    List<WalletGoal> findByWalletIdOrderByPriorityAsc(Long walletId);
}
