package com.avioconsulting.mule.vault.api.client.exception;

public class AccessException extends Exception {
    public AccessException(String errorMessage) {
        super(errorMessage);
    }
}
