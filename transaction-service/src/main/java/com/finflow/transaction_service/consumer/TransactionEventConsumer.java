package com.finflow.transaction_service.consumer;

import com.finflow.transaction_service.event.TransactionInitiatedEvent;
import com.finflow.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {
    private final TransactionService transactionService;

    @KafkaListener(
            topics = "${kafka.topics.transaction-initiated}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TransactionInitiatedEvent event){
        log.info("Received TransactionInitiatedEvent: id={}. type={}",
                event.getTransactionId(),event.getType());
        transactionService.processTransaction(event);
    }
}
