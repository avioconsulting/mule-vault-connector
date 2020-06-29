package com.avioconsulting.mule.connector.vault.provider.internal.operation;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import com.avioconsulting.mule.connector.vault.provider.internal.configuration.ConfigurationOverrides;
import com.avioconsulting.mule.connector.vault.provider.internal.configuration.VaultConfiguration;
import com.avioconsulting.mule.connector.vault.provider.api.VaultResponseAttributes;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultErrorTypeProvider;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

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
  @OutputJsonType(schema = "metadata/secret-schema.json")
  public Result<InputStream, VaultResponseAttributes> getSecret(@Config VaultConfiguration config,
                                                                @Connection VaultConnection connection,
                                                                @ParameterGroup(name = "Connector Overrides") ConfigurationOverrides overrides,
                                                                String path)
          throws DefaultMuleException, VaultAccessException, SecretNotFoundException, InterruptedException {
    if (overrides.getEngineVersion() != null) {
      logger.info(String.format("Getting secret at path [%s] with engine version [%d]", path, overrides.getEngineVersion().getEngineVersionNumber()));
    } else {
      logger.info("Engine Version is null!");
    }
    return connection.getSecret(path, overrides);
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
  @OutputJsonType(schema = "metadata/secret-schema.json")
  public Result<InputStream, VaultResponseAttributes> writeSecret(@Config VaultConfiguration config,
                                                                  @Connection VaultConnection connection,
                                                                  @ParameterGroup(name = "Connector Overrides") ConfigurationOverrides overrides,
                                                                  String path,
                                                                  String secret)
          throws DefaultMuleException, VaultAccessException, SecretNotFoundException, InterruptedException {
    return connection.writeSecret(path, secret, overrides);
  }

  /**
   * Encrypt data with the Vault transit secret engine
   *
   * @param connection a connected {@link VaultConnection}
   * @param transitMountpoint the mount point for the transit secret engine to use
   * @param keyName the key to use from the given endpoint
   * @param plaintext the plaintext to be encrypted - must be Base64 encoded
   * @return the encrypted value of the plaintext
   * @throws Exception if there is an issue from Vault
   */
  @Throws(VaultErrorTypeProvider.class)
  @MediaType(value = APPLICATION_JSON, strict = false)
  @OutputJsonType(schema = "metadata/encryption-schema.json")
  public Result<InputStream, VaultResponseAttributes> encryptData(@Config VaultConfiguration config,
                                                                  @Connection VaultConnection connection,
                                                                  @ParameterGroup(name = "Connector Overrides") ConfigurationOverrides overrides,
                                                                  String transitMountpoint,
                                                                  String keyName,
                                                                  String plaintext)
          throws DefaultMuleException, VaultAccessException, SecretNotFoundException, InterruptedException {
    return connection.encryptData(transitMountpoint, keyName, plaintext, overrides);
  }

  /**
   * Decrypyt data encrypted with the Vault secret engine
   *
   * @param connection a connected {@link VaultConnection}
   * @param transitMountpoint the mount point for the transit secret engine to use
   * @param keyName the key to use from the given endpoint
   * @param ciphertext the encrypted data to be decrypted
   * @return the Base64 encoded, decrypted value of the ciphertext
   * @throws Exception if there is an issue from Vault
   */
  @Throws(VaultErrorTypeProvider.class)
  @MediaType(value = APPLICATION_JSON, strict = false)
  @OutputJsonType(schema = "metadata/encryption-schema.json")
  public Result<InputStream, VaultResponseAttributes> decryptData(@Config VaultConfiguration config,
                                                                  @Connection VaultConnection connection,
                                                                  @ParameterGroup(name = "Connector Overrides") ConfigurationOverrides overrides,
                                                                  String transitMountpoint,
                                                                  String keyName,
                                                                  String ciphertext)
          throws DefaultMuleException, VaultAccessException, SecretNotFoundException, InterruptedException {
    return connection.decryptData(transitMountpoint, keyName, ciphertext, overrides);
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
  @Throws(VaultErrorTypeProvider.class)
  @MediaType(value = APPLICATION_JSON, strict = false)
  @OutputJsonType(schema = "metadata/encryption-schema.json")
  public Result<InputStream, VaultResponseAttributes> reencryptData(@Config VaultConfiguration config,
                                                                    @Connection VaultConnection connection,
                                                                    @ParameterGroup(name = "Connector Overrides") ConfigurationOverrides overrides,
                                                                    String transitMountpoint,
                                                                    String keyName,
                                                                    String ciphertext)
          throws DefaultMuleException, VaultAccessException, SecretNotFoundException, InterruptedException {
    return connection.reencryptData(transitMountpoint, keyName, ciphertext, overrides);
  }

}
