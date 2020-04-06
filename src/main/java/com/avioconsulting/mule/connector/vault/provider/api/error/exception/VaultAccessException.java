package com.avioconsulting.mule.connector.vault.provider.api.error.exception;

import org.mule.runtime.extension.api.exception.ModuleException;

public class VaultAccessException extends ModuleException {
    public VaultAccessException(Exception cause) {
        super(VaultErrorType.ACCESS_DENIED, cause);
    }
}
