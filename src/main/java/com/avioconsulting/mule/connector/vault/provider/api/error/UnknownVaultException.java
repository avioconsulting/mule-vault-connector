package com.avioconsulting.mule.connector.vault.provider.api.error;

import org.mule.runtime.extension.api.exception.ModuleException;

public class UnknownVaultException extends ModuleException {
    public UnknownVaultException(Exception cause) {
        super(VaultErrors.UNKNOWN_ERROR, cause);
    }
}
