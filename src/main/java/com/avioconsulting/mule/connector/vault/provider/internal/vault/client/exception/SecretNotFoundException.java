package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception;

public class SecretNotFoundException extends Exception {
    public SecretNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
