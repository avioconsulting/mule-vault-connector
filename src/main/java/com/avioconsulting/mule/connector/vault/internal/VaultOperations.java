package com.avioconsulting.mule.connector.vault.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import com.bettercloud.vault.VaultException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * Get a secret from Vault
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  public String getSecret(@Connection VaultConnection connection, String path) throws VaultException {

    try {
      Gson gson = new GsonBuilder().create();
      return gson.toJson(connection.getVault().logical().read(path).getData());
    } catch (VaultException ve) {
      LOGGER.error("Error retrieving secret", ve);
      throw ve;
    }

  }

  @MediaType(value = APPLICATION_JSON, strict = false)
  public void writeSecret(@Connection VaultConnection connection, String path, String secret) {
    try {
      Map<String,Object> secretData = new HashMap<>();
      connection.getVault().logical().write(path, secretData);
    } catch (VaultException ve) {
      LOGGER.error("Error writing secret", ve);
    }
  }

  /**
   * Example of a simple operation that receives a string parameter and returns a new string message that will be set on the payload.
          */
  @MediaType(value = ANY, strict = false)
  public String sayHi(String person) {
    return buildHelloMessage(person);
  }

  /**
   *
   * @return
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  public String stuff() {
    Gson gson = new GsonBuilder().create();
    Map<String,String> myStuff = new HashMap<>();
    myStuff.put("stuff","hello");
    return gson.toJson(myStuff);
  }

  /**
   * Private Methods are not exposed as operations
   */
  private String buildHelloMessage(String person) {
    return "Hello " + person + "!!!";
  }

}
