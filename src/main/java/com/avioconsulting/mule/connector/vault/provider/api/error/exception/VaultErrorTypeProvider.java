package com.avioconsulting.mule.connector.vault.provider.api.error.exception;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

public class VaultErrorTypeProvider implements ErrorTypeProvider {
    @Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        Set<ErrorTypeDefinition> errors = new HashSet<ErrorTypeDefinition>();

        errors.add(VaultErrorType.ACCESS_DENIED);
        errors.add(VaultErrorType.SECRET_NOT_FOUND);

        return errors;
    }
}
