package com.finflow.wallet_service.exception;

public class DuplicateTransactionException extends RuntimeException{
    public DuplicateTransactionException(String msg){
        super(msg);
    }
}
