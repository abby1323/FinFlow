package com.finflow.fraud_service.rules;

import com.finflow.fraud_service.event.TransactionInitiatedEvent;
import com.finflow.fraud_service.repository.FraudAlertRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HighFrequencyRule implements FraudRule{


    @Value("${fraud.rules.max-transactions-per-minute}")
    private int maxTransactionsPerMinute;

    @Override
    public Optional<String> evaluate(TransactionInitiatedEvent event) {
        return Optional.empty();
    }

    @Override
    public String getRuleName() {
        return "HIGH_FREQUENCY";
    }

    @Override
    public String getSeverity() {
        return "HIGH";
    }
}
