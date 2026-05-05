package com.kitlandvault.backend.repositories;

import com.kitlandvault.backend.entities.InternalTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalTransferRepository extends JpaRepository<InternalTransfer, Long> {

    List<InternalTransfer> findByUserIdAndStatusNotOrderByCreatedAtDesc(
            Long userId, InternalTransfer.Status status);

    List<InternalTransfer> findByUserIdOrderByCreatedAtDesc(Long userId);
}
