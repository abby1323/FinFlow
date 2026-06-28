package com.finflow.fraud_service.rules;

import com.finflow.fraud_service.event.TransactionInitiatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class HighAmountRule implements FraudRule{

    @Value("${fraud.rules.max-amount}")
    private BigDecimal maxAmount;

    @Override
    public Optional<String> evaluate(TransactionInitiatedEvent event) {
        if(event.getAmount().compareTo(maxAmount)>0){
            return Optional.of("Transaction amount "  + event.getAmount()
            + " exceeds threshold of " + maxAmount);
        }
        return Optional.empty();
    }

    @Override
    public String getRuleName() {
        return "HIGH_AMOUNT";
    }

    @Override
    public String getSeverity() {
        return "HIGH";
    }
}
