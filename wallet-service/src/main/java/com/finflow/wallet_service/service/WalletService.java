package com.finflow.wallet_service.service;

import com.finflow.wallet_service.dto.request.TopupRequest;
import com.finflow.wallet_service.dto.request.TransferRequest;
import com.finflow.wallet_service.dto.response.TransactionResponse;
import com.finflow.wallet_service.dto.response.WalletResponse;
import com.finflow.wallet_service.entity.Topup;
import com.finflow.wallet_service.entity.Wallet;
import com.finflow.wallet_service.event.TransactionInitiatedEvent;
import com.finflow.wallet_service.exception.DuplicateTransactionException;
import com.finflow.wallet_service.exception.InsufficientBalanceException;
import com.finflow.wallet_service.exception.WalletNotFoundException;
import com.finflow.wallet_service.repository.TopupRepo;
import com.finflow.wallet_service.repository.WalletRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepo walletRepo;
    private final TopupRepo topupRepo;
    private final RedisTemplate<String,Object> redisTemplate;
    private final KafkaTemplate<String, TransactionInitiatedEvent> kafkaTemplate;

    @Value("${kafka.topics.transaction-initiated}")
    private String transactionInitiatedTopic;

    private static final String BALANCE_CACHE_PREFIX = "wallet:balance:";
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final Duration BALANCE_TTL = Duration.ofSeconds(30);
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    @Transactional
    public WalletResponse createWallet(UUID userId){
        walletRepo.findByUserId(userId)
                .ifPresent(w->{
                    throw new DuplicateTransactionException(
                            "Wallet already exists for user: " + userId
                    );
                });
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .build();

        Wallet saved = walletRepo.save(wallet);
        log.info("Wallet created for userID={} , walletId={}",userId,saved.getId());

        return toWalletResponse(saved);
    }

    public WalletResponse getBalance(UUID userId){
        String cacheKey = BALANCE_CACHE_PREFIX + userId;

        // check redis first
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached!=null){
            log.debug("Cache hit for userId={}",userId);
            return (WalletResponse) cached;
        }

        // cache miss - fetch from db
        log.debug("Cache miss for userId={}, fetching from DB",userId);
        Wallet wallet = findWalletByUserId(userId);
        WalletResponse response = toWalletResponse(wallet);

        redisTemplate.opsForValue().set(cacheKey,response,BALANCE_TTL);

        return response;
    }

    @Transactional
    public TransactionResponse topup(TopupRequest request){

        // check idempotency key in redis
        checkIdempotency(request.getIdempotencyKey());

        // fetch wallet
        Wallet wallet = findWalletByUserId(request.getUserId());

        // credit balance
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepo.save(wallet);

        // record top-up
        Topup topup = new Topup().builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .idempotencyKey(request.getIdempotencyKey())
                .status("SUCCESS")
                .build();

        topupRepo.save(topup);

        // invalidate balance cache
        invalidateBalanceCache(request.getUserId());

        // mark idempotency key as used in redis
        markIdempotencyUsed(request.getIdempotencyKey());

        // publish event to kafka
        TransactionInitiatedEvent event = TransactionInitiatedEvent.builder()
                .transactionId(topup.getId())
                .receiverWalletId(wallet.getId())
                .amount(request.getAmount())
                .type("TOPUP")
                .currency(wallet.getCurrency())
                .initiatedAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(
                transactionInitiatedTopic,
                wallet.getId().toString(),event);
        log.info("TOPUP event published for walletId={}, amount={}", wallet.getId(), request.getAmount());

        return TransactionResponse.builder()
                .transactionId(topup.getId())
                .type("TOPUP")
                .amount(request.getAmount())
                .status("SUCCESS")
                .message("Wallet topped up successfully")
                .build();
    }


    @Transactional
    public TransactionResponse transfer(TransferRequest request){
        // check idempotency
        checkIdempotency(request.getIdempotencyKey());

        // prevent self-transfer
        if(request.getSenderUserId().equals(request.getReceiverUserId())){
            throw new IllegalArgumentException("Sender and receiver cannot be same");
        }

        // fetch both wallets
        Wallet sender = findWalletByUserId(request.getSenderUserId());
        Wallet receiver = findWalletByUserId(request.getReceiverUserId());

        // check insufficient balance
        if(sender.getBalance().compareTo(request.getAmount())<0){
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + sender.getBalance() +
                    ", Requested: " + request.getAmount());
        }

        // debit sender, credit receiver
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));
        walletRepo.save(sender);
        walletRepo.save(receiver);

        // invalidate both balance caches
        invalidateBalanceCache(request.getSenderUserId());
        invalidateBalanceCache(request.getReceiverUserId());

        //mark idempotency key as used
        markIdempotencyUsed(request.getIdempotencyKey());

        // generate transaction id for tracking
        UUID transactionId = UUID.randomUUID();

        // publish event to kafka
        TransactionInitiatedEvent event = TransactionInitiatedEvent.builder()
                .transactionId(transactionId)
                .senderWalletId(sender.getId())
                .receiverWalletId(receiver.getId())
                .amount(request.getAmount())
                .type("TRANSFER")
                .currency(sender.getCurrency())
                .initiatedAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(transactionInitiatedTopic,
                sender.getId().toString(), event);
        log.info("TRANSFER event published. sender={}, receiver={}, amount={}", sender.getId(),receiver.getId(),request.getAmount());

        return TransactionResponse.builder()
                .transactionId(transactionId)
                .type("TRANSFER")
                .amount(request.getAmount())
                .status("SUCCESS")
                .message("Transfer initiated successfully")
                .build();
    }

    private void markIdempotencyUsed(@NotNull(message = "Idempotency Key is required") String idempotencyKey) {
        redisTemplate.opsForValue().set(
                IDEMPOTENCY_PREFIX + idempotencyKey, "used", IDEMPOTENCY_TTL
        );
    }

    private void invalidateBalanceCache(@NotNull(message = "User ID is required") UUID userId) {
        redisTemplate.delete(BALANCE_CACHE_PREFIX+userId);
    }


    private void checkIdempotency(@NotNull(message = "Idempotency Key is required") String idempotencyKey) {
        String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;
        Boolean exists = redisTemplate.hasKey(redisKey);
        if(exists){
            throw new DuplicateTransactionException(
                    "Duplicate request detected for idempotency key: " + idempotencyKey
            );
        }
    }

    private Wallet findWalletByUserId(UUID userId) {
        return walletRepo.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for userId: " + userId));
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .build();
    }
}
