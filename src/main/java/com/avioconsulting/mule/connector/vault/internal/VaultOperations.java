package com.avioconsulting.mule.connector.vault.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import com.avioconsulting.mule.connector.vault.internal.connection.VaultConnection;
import com.bettercloud.vault.VaultException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;


/**
 * @author Adam Mead
 *
 * Operations provided by the Vault Extension are implemented by this class
 */
public class VaultOperations {

  private final Logger LOGGER = LoggerFactory.getLogger(VaultOperations.class);

  /**
   * Get a secret from Vault
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  public String getSecret(@Connection VaultConnection connection, String path) throws VaultException {

    try {
      Gson gson = new GsonBuilder().create();
      return gson.toJson(connection.getVault().logical().read(path).getData());
    } catch (VaultException ve) {
      LOGGER.error("Error retrieving secret from Vault", ve);
      throw ve;
    }

  }

  @MediaType(value = APPLICATION_JSON, strict = false)
  public void writeSecret(@Connection VaultConnection connection, String path, String secret) throws VaultException {
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


}
