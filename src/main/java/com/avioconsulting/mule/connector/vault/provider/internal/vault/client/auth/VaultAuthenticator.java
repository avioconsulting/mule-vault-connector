package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.AccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.VaultException;

public interface VaultAuthenticator {
    String authenticate(VaultConfig config) throws AccessException, VaultException, InterruptedException;

    String getAuthPath();
    String getAuthPayload(VaultConfig config) throws VaultException;
}
