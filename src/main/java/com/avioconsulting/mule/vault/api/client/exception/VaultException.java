package com.avioconsulting.mule.vault.api.client.exception;

public class VaultException extends Exception {

    int statusCode;

    public VaultException(int statusCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
