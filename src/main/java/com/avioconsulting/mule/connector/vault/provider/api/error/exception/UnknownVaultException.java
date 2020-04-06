package com.avioconsulting.mule.connector.vault.provider.api.error.exception;

import org.mule.runtime.extension.api.exception.ModuleException;

public class UnknownVaultException extends ModuleException {
    public UnknownVaultException(Exception cause) {
        super(VaultErrorType.UNKNOWN_ERROR, cause);
    }
}
