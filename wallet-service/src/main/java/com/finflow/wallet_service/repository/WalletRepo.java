package com.finflow.wallet_service.repository;

import com.finflow.wallet_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepo extends JpaRepository<Wallet,Long> {
    Optional<Wallet> findByUserId(UUID userId);
}
