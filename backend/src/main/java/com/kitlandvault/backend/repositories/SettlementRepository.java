package com.kitlandvault.backend.repositories;

import com.kitlandvault.backend.entities.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByCreditorIdAndDebtorIdOrderByCreatedAtDesc(Long creditorId, Long debtorId);

    List<Settlement> findByCreditorIdAndDebtorIdAndTypeAndStatus(
            Long creditorId, Long debtorId, Settlement.Type type, Settlement.Status status);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Settlement s " +
           "WHERE s.creditor.id = :creditorId AND s.debtor.id = :debtorId " +
           "AND s.type = :type AND s.status = :status")
    BigDecimal sumAmountByCreditorAndDebtorAndTypeAndStatus(
            @Param("creditorId") Long creditorId,
            @Param("debtorId") Long debtorId,
            @Param("type") Settlement.Type type,
            @Param("status") Settlement.Status status);
}
