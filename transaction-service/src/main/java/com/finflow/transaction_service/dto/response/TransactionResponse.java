package com.finflow.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amount;
    private String type;
    private String status;
    private String currency;
    private String failureReason;
    private LocalDateTime initiatedAt;
    private LocalDateTime processedAt;
}
