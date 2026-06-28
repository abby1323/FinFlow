package com.finflow.fraud_service.consumer;

import com.finflow.fraud_service.event.TransactionInitiatedEvent;
import com.finflow.fraud_service.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudEventConsumer {
    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(
            topics="${kafka.topics.transaction-initiated}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TransactionInitiatedEvent event){
        log.info("Received event for fraud analysis : id={}, type={}",
                event.getTransactionId(), event.getType());
        fraudDetectionService.analyzeTransaction(event);
    }
}
