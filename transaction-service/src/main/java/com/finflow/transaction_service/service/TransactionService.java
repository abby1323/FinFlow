package com.finflow.transaction_service.service;

import com.finflow.transaction_service.dto.response.TransactionResponse;
import com.finflow.transaction_service.event.NotificationEvent;
import com.finflow.transaction_service.event.TransactionInitiatedEvent;
import com.finflow.transaction_service.repository.TransactionRepo;
import com.finflow.transaction_service.entity.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepo transactionRepo;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-keys.notification}")
    private String notificationRoutingKey;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void processTransaction(TransactionInitiatedEvent event){
        log.info("Processing transaction: id={}, type={}, amount={}",
                event.getTransactionId(),event.getType(),event.getAmount());

        // check if already processed (idempotency guard)
        if(transactionRepo.existsById(event.getTransactionId())){
            log.warn("Transaction already processed, skipping : {}" , event.getTransactionId());
            return;
        }

        // Native insert - bypasses Hibernate entity state management entirely
        entityManager.createNativeQuery("""
            INSERT INTO transactions (id, sender_wallet_id, receiver_wallet_id,
                amount, type, status, currency, initiated_at, processed_at, created_at)
            VALUES (:id, :senderWalletId, :receiverWalletId,
                :amount, :type, :status, :currency, :initiatedAt, :processedAt, NOW())
            """)
                .setParameter("id", event.getTransactionId())
                .setParameter("senderWalletId", event.getSenderWalletId())
                .setParameter("receiverWalletId", event.getReceiverWalletId())
                .setParameter("amount", event.getAmount())
                .setParameter("type", event.getType())
                .setParameter("status", "COMPLETED")
                .setParameter("currency", event.getCurrency())
                .setParameter("initiatedAt", event.getInitiatedAt())
                .setParameter("processedAt", LocalDateTime.now())
                .executeUpdate();

        log.info("Transaction saved as COMPLETED: id={}", event.getTransactionId());

        // publish notification to RabbitMQ
        NotificationEvent notification = NotificationEvent.builder()
                .transactionId(event.getTransactionId())
                .walletId(event.getReceiverWalletId())
                .type(event.getType())
                .status("COMPLETED")
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .message(event.getType().equals("TOPUP")
                        ? "Your wallet has been topped up with "
                        + event.getCurrency() + " " + event.getAmount()
                        : "Transfer of " + event.getCurrency() + " "
                        + event.getAmount() + " completed successfully")
                .build();
        rabbitTemplate.convertAndSend(exchange,notificationRoutingKey,notification);
        log.info("Notification published to RabbitMQ for transactionId={}",
                event.getTransactionId());
    }

    public List<TransactionResponse> getTransactionsByWallet(UUID walletId){
        List<Transaction> sent = transactionRepo.findBySenderWalletIdOrderByCreatedAtDesc(walletId);
        List<Transaction> received = transactionRepo.findByReceiverWalletIdOrderByCreatedAtDesc(walletId);

        return java.util.stream.Stream.concat(sent.stream(),received.stream())
                .map(this::toResponse)
                .sorted((a,b)->
                        b.getInitiatedAt().compareTo(a.getInitiatedAt()))
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(UUID transactionId){
        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(()-> new RuntimeException("Transaction not found: " + transactionId));
        return toResponse(transaction);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .senderWalletId(transaction.getSenderWalletId())
                .receiverWalletId(transaction.getReceiverWalletId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .currency(transaction.getCurrency())
                .failureReason(transaction.getFailureReason())
                .initiatedAt(transaction.getInitiatedAt())
                .processedAt(transaction.getProcessedAt())
                .build();
    }

    private NotificationEvent buildNotification(Transaction transaction) {
        String message  = transaction.getType().equals("TOPUP")
                ? "Your wallet has been topped up with " + transaction.getCurrency()
                + " " + transaction.getAmount()
                : "Transfer of " + transaction.getCurrency() + " "
                + transaction.getAmount() + " completed successfully";
        return NotificationEvent.builder()
                .transactionId(transaction.getId())
                .walletId(transaction.getReceiverWalletId())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .message(message)
                .build();
    }











}
