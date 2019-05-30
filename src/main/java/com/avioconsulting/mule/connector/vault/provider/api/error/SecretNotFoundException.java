package com.avioconsulting.mule.connector.vault.provider.api.error;

import org.mule.runtime.extension.api.exception.ModuleException;

public class SecretNotFoundException extends ModuleException {
    public SecretNotFoundException(Exception cause) {
        super(VaultErrors.SECRET_NOT_FOUND, cause);
    }
}
