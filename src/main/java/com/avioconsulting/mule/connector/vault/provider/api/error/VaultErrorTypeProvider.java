package com.avioconsulting.mule.connector.vault.provider.api.error;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

public class VaultErrorTypeProvider implements ErrorTypeProvider {
    @Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        Set<ErrorTypeDefinition> errors = new HashSet<>();

        errors.add(VaultErrors.ACCESS_DENIED);
        errors.add(VaultErrors.SECRET_NOT_FOUND);
        errors.add(VaultErrors.UNKNOWN_ERROR);

        return errors;
    }
}
