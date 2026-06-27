package com.finflow.transaction_service.repository;

import com.finflow.transaction_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, UUID> {
    List<Transaction> findBySenderWalletIdOrderByCreatedAtDesc(UUID senderId);
    List<Transaction> findByReceiverWalletIdOrderByCreatedAtDesc(UUID receiverId);
    List<Transaction> findByStatus(String status);
}
