package com.avioconsulting.mule.connector.vault.provider.internal.operation;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.UnknownVaultException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultErrorTypeProvider;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author Adam Mead
 *
 * Operations provided by the Vault Extension are implemented by this class
 */
public class VaultOperations {

  private static final Logger logger = LoggerFactory.getLogger(VaultOperations.class);

  /**
   * Retrieve a secret from Vault
   *
   * @param connection a connected {@link VaultConnection}
   * @param path path to the secret that will be read, starting with the secret engine path (i.e. secret/myOrg/myProject/mySecret)
   * @return JSON value of secret
   * @throws Exception if there is an issue from Vault
   */
  @Throws(VaultErrorTypeProvider.class)
  @MediaType(value = APPLICATION_JSON, strict = false)
  public String getSecret(@Connection VaultConnection connection, String path) throws VaultAccessException, SecretNotFoundException, UnknownVaultException {
    return connection.getSecret(path);
  }

  /**
   * Write a secret to Vault
   *
   * @param connection a connected {@link VaultConnection}
   * @param path path to which the secret will be written, starting with the secret engine path (i.e. secret/myOrg/myProject/mySecret)
   * @param secret the secret data in JSON format
   * @throws Exception if there is an issue from Vault
   */
  @Throws(VaultErrorTypeProvider.class)
  @MediaType(value = APPLICATION_JSON, strict = false)
  public void writeSecret(@Connection VaultConnection connection, String path, String secret) throws VaultAccessException, UnknownVaultException {
    connection.writeSecret(path, secret);
  }

  /**
   * Encrypt data with the Vault transit secret engine
   *
   * @param connection a connected {@link VaultConnection}
   * @param transitMountpoint the mount point for the transit secret engine to use
   * @param keyName the key to use from the given endpoint
   * @param plaintext the plaintext to be encrypted
   * @return the encrypted value of the plaintext
   * @throws Exception if there is an issue from Vault
   */
  @Throws(VaultErrorTypeProvider.class)
  @MediaType(value = ANY, strict = false)
  public String encryptData(@Connection VaultConnection connection, String transitMountpoint, String keyName, String plaintext) throws VaultAccessException, UnknownVaultException {
    return connection.encryptData(transitMountpoint, keyName, plaintext);
  }

  /**
   * Decrypyt data encrypted with the Vault secret engine
   *
   * @param connection a connected {@link VaultConnection}
   * @param transitMountpoint the mount point for the transit secret engine to use
   * @param keyName the key to use from the given endpoint
   * @param ciphertext the encrypted data to be decrypted
   * @return the decrypted value of the ciphertext
   * @throws Exception if there is an issue from Vault
   */
  @MediaType(value = ANY, strict = false)
  public String decryptData(@Connection VaultConnection connection, String transitMountpoint, String keyName, String ciphertext) throws VaultAccessException, UnknownVaultException {
    return connection.decryptData(transitMountpoint, keyName, ciphertext);
  }

  /**
   * Reencrypt the given ciphertext with the newest version of the encryption key
   *
   * @param connection a connected {@link VaultConnection}
   * @param transitMountpoint the mount point for the transit secret engine to use
   * @param keyName the key to use from the given endpoint
   * @param ciphertext the encrypted data to be re-encrypted with the new key
   * @return the re-encrypted ciphertext
   * @throws Exception if there is an issue from Vault
   */
  @MediaType(value = ANY, strict = false)
  public String reencryptData(@Connection VaultConnection connection, String transitMountpoint, String keyName, String ciphertext) throws VaultAccessException, UnknownVaultException {
    return connection.reencryptData(transitMountpoint, keyName, ciphertext);
  }

}
