package com.finflow.wallet_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "topups")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Topup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false,nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id",nullable = false)
    private Wallet wallet;

    @Column(nullable = false,precision = 19,scale = 4)
    private BigDecimal amount;

    @Column(name = "idempotency_key",nullable = false,unique = true,length = 100)
    private String idempotencyKey;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected  void  onCreate(){
        createdAt = LocalDateTime.now();
        if(status==null) status="SUCCESS";
    }
}

