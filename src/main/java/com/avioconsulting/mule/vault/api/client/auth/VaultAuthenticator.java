package com.avioconsulting.mule.vault.api.client.auth;

import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;

public interface VaultAuthenticator {
    String authenticate(VaultConfig config) throws AccessException, VaultException, InterruptedException;
}
