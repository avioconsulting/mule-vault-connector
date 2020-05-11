package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;


import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;

import com.avioconsulting.mule.vault.api.client.VaultConfig;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A connection to Vault using Token Authentication
 *
 * @author Adam Mead
 */
public final class BasicVaultConnection extends AbstractVaultConnection {

  private static final Logger logger = LoggerFactory.getLogger(BasicVaultConnection.class);


/* *************************************** */
/*  Java Vault API classes no longer used. */
/* *************************************** */
//  public BasicVaultConnection(String id, String vaultToken, String vaultUrl, SSLProperties sslProperties,
//                              EngineVersion engineVersion) throws ConnectionException {
//    this.id = id;
//    try {
//      this.vaultConfig = new VaultConfig().address(vaultUrl);
//      if (engineVersion != null) {
//        this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
//      }
//      SslConfig ssl = getVaultSSLConfig(sslProperties);
//      System.out.println("BasicVaultConnection: trust store file: " + sslProperties.getTrustStoreFile());
//      this.vault = new Vault(this.vaultConfig.token(vaultToken).sslConfig(ssl.build()).build());
//      LookupResponse lookupResponse = this.vault.auth().lookupSelf();
//      renewable = lookupResponse.isRenewable();
//      long creationTimeSec = lookupResponse.getCreationTime();
//      long ttl = lookupResponse.getTTL();
//
//      if (creationTimeSec > 0) {
//        Instant creationTime = Instant.ofEpochSecond(creationTimeSec);
//        if (ttl > 0) {
//          this.expirationTime = creationTime.plusSeconds(ttl);
//        } else {
//          this.expirationTime = null;
//        }
//      }
//
//      this.valid = true;
//    } catch (VaultException ve) {
//      logger.error("Error establishing Vault connection", ve);
//      throw new ConnectionException(ve.getMessage(), ve.getCause());
//    }
//  }

  /**
   * Construct a connection using a Vault Token
   *
   * @param vaultToken     Token to use for authentication
   * @param vaultUrl       URL for the Vault server (https://host:port)
   * @param httpClient     HttpClient to use to make the connection
   * @param engineVersion  The version of the secret engine to use, defaulting to Version 2
   */
  public BasicVaultConnection(String vaultToken, String vaultUrl, HttpClient httpClient, EngineVersion engineVersion) {
    this.client = httpClient;
    this.token = vaultToken;
    this.vaultUrl = vaultUrl;
    this.engineVersion = engineVersion;

    this.vConfig = new VaultConfig(httpClient, vaultUrl, 30, vaultToken, engineVersion.getEngineVersionNumber());
  }

  @Override
  public boolean isValid() {
    boolean valid = false;
    HttpRequestBuilder builder = HttpRequest.builder();
    builder.uri(vaultUrl + "/v1/auth/token/lookup" );
    builder.addHeader("X-Vault-Token", token);
    builder.method(HttpConstants.Method.GET);
    logger.info("isValid() " + builder.build().toString());
    CompletableFuture<HttpResponse> completable = client.sendAsync(builder.build(), 500, true, null);

    try {
      HttpResponse response = completable.get();

      logger.info("isValid() Response: " + response.getStatusCode() + " " + response.toString());
      if (response.getStatusCode() == 404) {
        logger.error("Secret not found in Vault");
      } else if (response.getStatusCode() == 403) {
        logger.error("Access denied in Vault");
      } else if (response.getStatusCode() > 299){
        logger.error("Unknown Vault Exception");
      } else {
        valid = true;
      }
    } catch (InterruptedException e) {
      logger.error("Timeout", e);
    } catch (ExecutionException e) {
      logger.error("Execution Exception", e);
    }

    return valid;
  }
}
