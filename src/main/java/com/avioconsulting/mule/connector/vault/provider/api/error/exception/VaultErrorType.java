package com.avioconsulting.mule.connector.vault.provider.api.error.exception;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public enum VaultErrorType implements ErrorTypeDefinition<VaultErrorType> {
   ACCESS_DENIED,
   SECRET_NOT_FOUND,
   UNKNOWN_ERROR
}
