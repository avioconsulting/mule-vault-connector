package com.avioconsulting.mule.connector.vault.provider.internal.error.provider;

import static com.avioconsulting.mule.connector.vault.provider.internal.error.VaultErrorType.ACCESS_DENIED;
import static com.avioconsulting.mule.connector.vault.provider.internal.error.VaultErrorType.SECRET_NOT_FOUND;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VaultErrorTypeProvider implements ErrorTypeProvider {
    @Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ACCESS_DENIED, SECRET_NOT_FOUND)));
    }
}
