package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception;

public class VaultException extends Exception {

    final int statusCode;

    public VaultException(int statusCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
    }

    public VaultException(Exception e) {
        super(e);
        statusCode = -1;
    }

    public VaultException(String errorMessage) {
        super(errorMessage);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
