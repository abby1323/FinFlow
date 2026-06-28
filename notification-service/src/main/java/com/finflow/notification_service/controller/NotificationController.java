package com.finflow.notification_service.controller;

import com.finflow.notification_service.entity.NotificationLog;
import com.finflow.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<NotificationLog>> getByWallet(@PathVariable UUID walletId){
        return ResponseEntity.ok(notificationService.getNotificationsByWallet(walletId));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<NotificationLog>> getByTransaction(@PathVariable UUID transactionId){
        return ResponseEntity.ok(notificationService.getNotificationsByTransaction(transactionId));
    }
}
