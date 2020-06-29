package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.configuration.VaultConfiguration;
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
   *
   * @param config
   * @throws DefaultMuleException
   * @throws InterruptedException
   */
  public BasicVaultConnection(VaultConfig config) throws DefaultMuleException, InterruptedException {
    super(config);
    this.vault = new VaultClient(this.config);

    try {
      logger.info("Authenticating");
      this.vault.authenticate();
      this.validConnection = true;
      logger.info("Authentication successful");
    } catch (AccessException e) {
      logger.error("Authentication failed", e);
      throw new VaultAccessException(e);
    } catch (VaultException e) {
      logger.error("Authentication failed", e);
      throw new DefaultMuleException(e);
    }
  }
}
