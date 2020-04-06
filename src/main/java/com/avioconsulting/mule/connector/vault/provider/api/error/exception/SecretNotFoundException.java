package com.avioconsulting.mule.connector.vault.provider.api.error.exception;

import org.mule.runtime.extension.api.exception.ModuleException;

public class SecretNotFoundException extends ModuleException {
    public SecretNotFoundException(Exception cause) {
        super(VaultErrorType.SECRET_NOT_FOUND, cause);
    }
}
