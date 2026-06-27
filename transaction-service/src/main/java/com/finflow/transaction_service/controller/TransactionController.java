package com.finflow.transaction_service.controller;

import com.finflow.transaction_service.dto.response.TransactionResponse;
import com.finflow.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // GET /api/v1/transactions/{transactionId}
    @GetMapping("{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID transactionId){
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    // GET /api/v1/transactions/wallet/{walletId]
    public ResponseEntity<List<TransactionResponse>> getWalletTransactions(@PathVariable UUID walletId){
        return ResponseEntity.ok(transactionService.getTransactionsByWallet(walletId));
    }
}
