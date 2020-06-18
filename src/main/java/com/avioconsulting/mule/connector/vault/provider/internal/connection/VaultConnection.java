package com.avioconsulting.mule.connector.vault.provider.internal.connection;

import com.avioconsulting.mule.connector.vault.provider.api.VaultResponseAttributes;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.UnknownVaultException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.internal.configuration.ConfigurationOverrides;
import com.bettercloud.vault.Vault;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;

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

    Result<InputStream, VaultResponseAttributes> getSecret(String path, ConfigurationOverrides overrides) throws VaultAccessException, SecretNotFoundException, UnknownVaultException;

    Result<InputStream, VaultResponseAttributes> writeSecret(String path, String secret, ConfigurationOverrides overrides) throws VaultAccessException, UnknownVaultException;

    Result<InputStream, VaultResponseAttributes> encryptData(String transitMountpoint, String keyName, String plaintext, ConfigurationOverrides overrides) throws VaultAccessException, UnknownVaultException;

    Result<InputStream, VaultResponseAttributes> decryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws VaultAccessException, UnknownVaultException;

    Result<InputStream, VaultResponseAttributes> reencryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws VaultAccessException, UnknownVaultException;
}
