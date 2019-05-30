package com.avioconsulting.mule.connector.vault.provider.api.error;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public enum VaultErrors implements ErrorTypeDefinition<VaultErrors> {
   ACCESS_DENIED,
   SECRET_NOT_FOUND,
   UNKNOWN_ERROR
}
