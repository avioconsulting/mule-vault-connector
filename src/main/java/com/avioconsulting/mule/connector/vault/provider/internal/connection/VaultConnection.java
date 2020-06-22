package com.avioconsulting.mule.connector.vault.provider.internal.connection;

import com.avioconsulting.mule.connector.vault.provider.api.VaultResponseAttributes;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.configuration.ConfigurationOverrides;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;

/**
 * Vault Connection Interface used for all Vault Connections
 *
 * @author Adam Mead
 */
public interface VaultConnection {

    void invalidate();

    boolean isValid() throws DefaultMuleException;

    Result<InputStream, VaultResponseAttributes> getSecret(String path, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException;

    Result<InputStream, VaultResponseAttributes> writeSecret(String path, String secret, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException;

    Result<InputStream, VaultResponseAttributes> encryptData(String transitMountpoint, String keyName, String plaintext, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException;

    Result<InputStream, VaultResponseAttributes> decryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException;

    Result<InputStream, VaultResponseAttributes> reencryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException;
}
