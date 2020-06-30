package com.avioconsulting.mule.connector.vault.provider.internal.error;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

import java.util.Optional;

public enum VaultErrorType implements ErrorTypeDefinition<VaultErrorType> {
   ACCESS_DENIED(MuleErrors.CLIENT_SECURITY),
   SECRET_NOT_FOUND;

   private ErrorTypeDefinition<? extends Enum<?>> parent;

   VaultErrorType(ErrorTypeDefinition<? extends Enum<?>> parent) {
      this.parent = parent;
   }

   VaultErrorType() {};

   @Override
   public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
      return Optional.ofNullable(parent);
   }
}
