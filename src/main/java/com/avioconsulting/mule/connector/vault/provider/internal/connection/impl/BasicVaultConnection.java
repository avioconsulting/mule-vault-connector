package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.internal.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultClient;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.AccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.VaultException;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      throw new VaultAccessException(e);
    } catch (VaultException e) {
      throw new DefaultMuleException(e);
    }
  }
}
