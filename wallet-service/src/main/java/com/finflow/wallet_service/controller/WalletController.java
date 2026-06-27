package com.finflow.wallet_service.controller;

import com.finflow.wallet_service.dto.request.TopupRequest;
import com.finflow.wallet_service.dto.request.TransferRequest;
import com.finflow.wallet_service.dto.response.TransactionResponse;
import com.finflow.wallet_service.dto.response.WalletResponse;
import com.finflow.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // POST /api/v1/wallets/{userId}
    @PostMapping("/{userId}")
    public ResponseEntity<WalletResponse> createWallet(@PathVariable UUID userId){
        WalletResponse response = walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // GET /api/v1/wallets/{userId}/balance
    @GetMapping("/{userId}/balance")
    public ResponseEntity<WalletResponse> getBalance(@PathVariable UUID userId){
        WalletResponse response = walletService.getBalance(userId);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/wallets/topup
    @PostMapping("/topup")
    public ResponseEntity<TransactionResponse> topup(
            @Valid @RequestBody TopupRequest request){
        TransactionResponse response = walletService.topup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/v1/wallets/transfer
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request){
        TransactionResponse response = walletService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
