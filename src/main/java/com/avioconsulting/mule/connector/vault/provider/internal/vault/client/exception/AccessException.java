package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception;

public class AccessException extends Exception {
    public AccessException(String errorMessage) {
        super(errorMessage);
    }
}
