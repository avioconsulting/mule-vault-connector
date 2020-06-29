package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.vault.api.client.VaultClient;
import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.auth.TokenAuthenticator;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A connection to Vault using Token Authentication
 *
 * @author Adam Mead
 */
public final class BasicVaultConnection extends AbstractVaultConnection {

  private static final Logger logger = LoggerFactory.getLogger(BasicVaultConnection.class);

  /**
   * Construct a connection using a Vault Token
   *
   * @param vaultToken     Token to use for authentication
   * @param vaultUrl       URL for the Vault server (https://host:port)
   * @param httpClient     HttpClient to use to make the connection
   */
  public BasicVaultConnection(String vaultToken, String vaultUrl, HttpClient httpClient, Integer responseTimeout, TimeUnit responseTimeoutUnit, Boolean followRedirects) throws DefaultMuleException, InterruptedException {
    this.config = VaultConfig.builder().
            authenticator(new TokenAuthenticator(vaultToken)).
            httpClient(httpClient).
            baseUrl(vaultUrl).
            timeout(responseTimeout).
            timeoutUnit(responseTimeoutUnit).
            followRedirects(followRedirects).
            build();
    this.vault = new VaultClient(this.config);

    try {
      this.vault.authenticate();
      this.validConnection = true;
    } catch (AccessException e) {
      throw new VaultAccessException(e);
    } catch (VaultException e) {
      throw new DefaultMuleException(e);
    }
  }
}
