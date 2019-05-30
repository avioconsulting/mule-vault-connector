package com.avioconsulting.mule.connector.vault.provider.api.error;

import org.mule.runtime.extension.api.exception.ModuleException;

public class VaultAccessException extends ModuleException {
    public VaultAccessException(Exception cause) {
        super(VaultErrors.ACCESS_DENIED, cause);
    }
}
