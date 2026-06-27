package com.finflow.wallet_service.repository;

import com.finflow.wallet_service.entity.Topup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopupRepo extends JpaRepository<Topup, UUID> {
    Optional<Topup> findByIdempotencyKey(String idempotencyKey);
}
