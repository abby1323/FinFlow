package com.finflow.fraud_service.rules;

import com.finflow.fraud_service.event.TransactionInitiatedEvent;

import java.util.Optional;

public interface FraudRule {
    Optional<String> evaluate(TransactionInitiatedEvent event);
    String getRuleName();
    String getSeverity();
}
