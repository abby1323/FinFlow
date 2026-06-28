package com.finflow.fraud_service.service;


import com.finflow.fraud_service.entity.FraudAlert;
import com.finflow.fraud_service.event.TransactionInitiatedEvent;
import com.finflow.fraud_service.repository.FraudAlertRepo;
import com.finflow.fraud_service.rules.FraudRule;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {
    private final FraudAlertRepo fraudAlertRepo;
    private final List<FraudRule> fraudRules;

    @Transactional
    public void analyzeTransaction(TransactionInitiatedEvent event){
        log.info("Analyzing transaction for fraud: id={},type={}, amount={} ",
                event.getTransactionId(),event.getType(),event.getAmount());
        UUID walletId = event.getSenderWalletId() !=null
                ? event.getSenderWalletId()
                : event.getReceiverWalletId();
        for(FraudRule rule: fraudRules){
            Optional<String> violation = rule.evaluate(event);
            if(violation.isPresent()){
                FraudAlert alert =  FraudAlert.builder()
                        .transactionId(event.getTransactionId())
                        .walletId(walletId)
                        .amount(event.getAmount())
                        .type(event.getType())
                        .ruleTriggered(rule.getRuleName())
                        .severity(rule.getSeverity())
                        .status("OPEN")
                        .details(violation.get())
                        .build();

                fraudAlertRepo.save(alert);
                log.warn("FRAUD ALERT - rule={}, transactionId={}, details={}",
                        rule.getRuleName(),event.getTransactionId(),violation.get());
            }
        }
        log.info("Fraud analysis completed for transactionId={}",event.getTransactionId());
    }

    public List<FraudAlert> getAlertsByWallet(UUID walletId){
        return fraudAlertRepo.findByWalletIdOrderByCreatedAtDesc(walletId);
    }

    public List<FraudAlert> getOpenAlerts(){
        return fraudAlertRepo.findByStatus("OPEN");
    }
}
