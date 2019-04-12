package com.avioconsulting.mule.connector.vault.provider.api;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import com.avioconsulting.mule.connector.vault.provider.api.connection.VaultConnection;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Adam Mead
 *
 * Operations provided by the Vault Extension are implemented by this class
 */
public class VaultOperations {

  private final Logger LOGGER = LoggerFactory.getLogger(VaultOperations.class);

  /**
   * Retrieve a secret from Vault
   *
   * @param connection a connected {@link VaultConnection}
   * @param path path to the secret that will be read, starting with the secret engine path (i.e. secret/myOrg/myProject/mySecret)
   * @return JSON value of secret
   * @throws Exception if there is an issue from Vault
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  public String getSecret(@Connection VaultConnection connection, String path) throws Exception {

    try {
      Gson gson = new GsonBuilder().create();
      return gson.toJson(connection.getVault().logical().read(path).getData());
    } catch (VaultException ve) {
      LOGGER.error("Error retrieving secret from Vault", ve);
      throw ve;
    }

  }

  /**
   * Write a secret to Vault
   *
   * @param connection a connected {@link VaultConnection}
   * @param path path to which the secret will be written, starting with the secret engine path (i.e. secret/myOrg/myProject/mySecret)
   * @param secret the secret data in JSON format
   * @throws Exception if there is an issue from Vault
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  public void writeSecret(@Connection VaultConnection connection, String path, String secret) throws Exception {
    try {
      Gson gson = new Gson();
      Type secretType = new TypeToken<Map<String,Object>>(){}.getType();
      Map<String,Object> secretData = gson.fromJson(secret, secretType);
      connection.getVault().logical().write(path, secretData);
    } catch (VaultException ve) {
      LOGGER.error("Error writing secret to Vault", ve);
      throw ve;
    }
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
  @MediaType(value = ANY, strict = false)
  public String encryptData(@Connection VaultConnection connection, String transitMountpoint, String keyName, String plaintext) throws Exception {
    try {
      Map<String, Object> data = new HashMap<>();
      data.put("plaintext", Base64.getEncoder().encodeToString(plaintext.getBytes()));
      LogicalResponse response = connection.getVault().logical().write(transitMountpoint + "/encrypt/" + keyName, data);
      return response.getData().get("ciphertext");
    } catch (VaultException ve) {
      LOGGER.error("Error encrypting data with Vault", ve);
      throw ve;
    }
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
  public String decryptData(@Connection VaultConnection connection, String transitMountpoint, String keyName, String ciphertext) throws Exception {
    try {
      Map<String, Object> data = new HashMap<>();
      data.put("ciphertext", ciphertext);
      System.out.println("Path: " + transitMountpoint + "/decrypt/" + keyName);
      LogicalResponse response = connection.getVault().logical().write(transitMountpoint + "/decrypt/" + keyName, data);
      return response.getData().get("plaintext");
    } catch (VaultException ve) {
      LOGGER.error("Error encrypting data with Vault", ve);
      throw ve;
    }
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
  public String reencryptData(@Connection VaultConnection connection, String transitMountpoint, String keyName, String ciphertext) throws Exception {
    try {
      Map<String, Object> data = new HashMap<>();
      data.put("ciphertext", ciphertext);
      System.out.println("Path: " + transitMountpoint + "/rewrap/" + keyName);
      LogicalResponse response = connection.getVault().logical().write(transitMountpoint + "/rewrap/" + keyName, data);
      return response.getData().get("ciphertext");
    } catch (VaultException ve) {
      LOGGER.error("Error encrypting data with Vault", ve);
      throw ve;
    }
  }

}
