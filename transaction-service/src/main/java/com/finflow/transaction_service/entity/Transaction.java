package com.finflow.transaction_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false,nullable = false)
    private UUID id;

    @Column(name = "sender_wallet_id")
    private UUID senderWalletId;

    @Column(name = "receiver_wallet_id",nullable = false)
    private UUID receiverWalletId;

    @Column(nullable = false,precision = 19,scale = 4)
    private BigDecimal amount;

    @Column(nullable = false,length = 20)
    private String type;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false,length = 3)
    private String currency;

    @Column(name = "failure_reason",length = 255)
    private String failureReason;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;


    @PrePersist
    protected  void onCreate(){
        createdAt = LocalDateTime.now();
        if(status==null) status="PENDING";
    }

}

