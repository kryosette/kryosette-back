package com.example.demo.exceptions;

public class TaxAccountNotFoundException extends RuntimeException {
    public TaxAccountNotFoundException(String message) {
        super(message);
    }
}
