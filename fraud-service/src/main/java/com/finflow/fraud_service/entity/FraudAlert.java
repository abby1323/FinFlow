package com.finflow.fraud_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transaction_id" , nullable = false)
    private UUID transactionId;

    @Column(name = "wallet_id",nullable = false)
    private UUID walletId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "rule_triggered", nullable = false,length = 100)
    private String ruleTriggered;

    @Column(nullable = false,length = 20)
    private String severity;

    @Column(nullable = false,length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        if(severity==null) severity="MEDIUM";
        if(status==null) status="OPEN";
    }
}
