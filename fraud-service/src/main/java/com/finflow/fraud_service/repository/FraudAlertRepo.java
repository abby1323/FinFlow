package com.finflow.fraud_service.repository;

import com.finflow.fraud_service.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudAlertRepo extends JpaRepository<FraudAlert,UUID> {
    List<FraudAlert> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
    List<FraudAlert> findByStatus(String status);
    long countByWalletIdAndCreatedAtAfter(UUID walletId, LocalDateTime oneMinuteAgo);
    @Query("SELECT COUNT(DISTINCT f.transactionId) FROM FraudAlert f " +
    "WHERE f.walletId = : walletId AND f.createdAt > :after")
    long countDistinctTransactionByWalletIdAndCreatedAtAfter(
            @Param("walletId") UUID walletId,
            @Param("after") LocalDateTime oneMinuteAgo);
}
