package com.finflow.wallet_service.dto.response;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID transactionId;
    private String type;
    private BigDecimal amount;
    private String status;
    private String message;
}
