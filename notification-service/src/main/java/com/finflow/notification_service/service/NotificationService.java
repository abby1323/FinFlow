package com.finflow.notification_service.service;

import com.finflow.notification_service.entity.NotificationLog;
import com.finflow.notification_service.event.NotificationEvent;
import com.finflow.notification_service.repository.NotificationLogRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepo notificationLogRepo;

    @Transactional
    public void processNotification(NotificationEvent event){
        log.info("Processing notification for transactionId={},type={}, walletId={}",
                event.getTransactionId(),event.getType(),event.getWalletId());

        // simulate sending notification (email/SMS)
        sendNotification(event);

        // log delivery to db
        NotificationLog logEntry = NotificationLog.builder()
                .transactionId(event.getTransactionId())
                .walletId(event.getWalletId())
                .type(event.getType())
                .status(event.getStatus())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .message(event.getMessage())
                .deliveryStatus("DELIVERED")
                .build();

        notificationLogRepo.save(logEntry);
        log.info("Notification logged to DB for transactionId={}", event.getTransactionId());

    }

    public List<NotificationLog> getNotificationsByWallet(UUID walletId){
        return notificationLogRepo.findByWalletIdOrderByDeliveredAtDesc(walletId);
    }

    public List<NotificationLog> getNotificationsByTransaction(UUID transactionId){
        return notificationLogRepo.findByTransactionId(transactionId);
    }


    private void sendNotification(NotificationEvent event) {
        // simulate email/sms delivery
        log.info("NOTIFICATION SENT -> walletId={} | type = {} | message = '{}'",
                event.getWalletId(),event.getType(),event.getMessage());
    }
}
