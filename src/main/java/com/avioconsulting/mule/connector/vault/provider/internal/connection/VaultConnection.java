package com.avioconsulting.mule.connector.vault.provider.internal.connection;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.UnknownVaultException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.bettercloud.vault.Vault;

/**
 * Vault Connection Interface used for all Vault Connections
 *
 * @author Adam Mead
 */
public interface VaultConnection {

    String getId();

    Vault getVault();

    void invalidate();

    boolean isValid();

    void renewLease();

    String getSecret(String path) throws VaultAccessException, SecretNotFoundException, UnknownVaultException;

    void writeSecret(String path, String secret) throws VaultAccessException, UnknownVaultException;

    String encryptData(String transitMountpoint, String keyName, String plaintext) throws VaultAccessException, UnknownVaultException;

    String decryptData(String transitMountpoint, String keyName, String ciphertext) throws VaultAccessException, UnknownVaultException;

    String reencryptData(String transitMountpoint, String keyName, String ciphertext) throws VaultAccessException, UnknownVaultException;
}
