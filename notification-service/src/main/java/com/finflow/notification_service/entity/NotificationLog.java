package com.finflow.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name="transaction_id",nullable = false)
    private UUID transactionId;

    @Column(name = "wallet_id",nullable = false)
    private UUID walletId;

    @Column(nullable = false,length = 20)
    private String type;

    @Column(nullable = false,length = 20)
    private String status;

    @Column(nullable = false,length = 3)
    private String currency;

    @Column(nullable = false, precision = 19,scale = 4)
    private BigDecimal amount;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String message;

    @Column(name = "delivered_at",nullable = false)
    private LocalDateTime deliveredAt;

    @Column(name = "delivery_status",nullable = false, length = 20)
    private String deliveryStatus;

    @PrePersist
    protected void onCreate(){
        if(deliveredAt==null)
            deliveredAt=LocalDateTime.now();
        if(deliveryStatus==null)
            deliveryStatus="DELIVERED";
    }

}

