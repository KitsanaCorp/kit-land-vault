package com.kitlandvault.backend.repositories;

import com.kitlandvault.backend.entities.GroupTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupTransactionRepository extends JpaRepository<GroupTransaction, Long> {

    List<GroupTransaction> findByGroupIdOrderByTransactionDateDescIdDesc(Long groupId);
}
