package com.avioconsulting.mule.vault.api.client.exception;

public class SecretNotFoundException extends Exception {
    public SecretNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
