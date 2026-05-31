package com.kitlandvault.backend.repositories;

import com.kitlandvault.backend.entities.GroupExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupExpenseSplitRepository extends JpaRepository<GroupExpenseSplit, Long> {

    List<GroupExpenseSplit> findByUserIdAndStatus(Long userId, GroupExpenseSplit.Status status);

    @Query("SELECT s FROM GroupExpenseSplit s JOIN s.groupTransaction t WHERE t.group.id = :groupId AND s.status = :status")
    List<GroupExpenseSplit> findByGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") GroupExpenseSplit.Status status);

    @Query("SELECT s FROM GroupExpenseSplit s JOIN s.groupTransaction t WHERE t.group.id = :groupId AND s.user.id = :userId")
    List<GroupExpenseSplit> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
