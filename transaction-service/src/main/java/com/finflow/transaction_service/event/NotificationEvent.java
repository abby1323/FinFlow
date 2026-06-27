package com.finflow.transaction_service.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID transactionId;
    private UUID walletId;
    private String type;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String message;
}
