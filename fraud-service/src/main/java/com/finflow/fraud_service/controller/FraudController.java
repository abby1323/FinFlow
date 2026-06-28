package com.finflow.fraud_service.controller;


import com.finflow.fraud_service.entity.FraudAlert;
import com.finflow.fraud_service.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudController {
    private final FraudDetectionService fraudDetectionService;

    @GetMapping("/alerts")
    public ResponseEntity<List<FraudAlert>> getOpenAlerts(){
        return ResponseEntity.ok(fraudDetectionService.getOpenAlerts());
    }

    @GetMapping("/alerts/wallet/{walletId}")
    public ResponseEntity<List<FraudAlert>> getAlertsByWalletId(@PathVariable UUID walletId){
        return ResponseEntity.ok(fraudDetectionService.getAlertsByWallet(walletId));
    }
}
