package com.avioconsulting.mule.connector.vault.provider.api.connection.impl;


import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * A connection to Vault using Token Authentication
 *
 * @author Adam Mead
 */
public final class BasicVaultConnection extends AbstractVaultConnection {

  private final Logger LOGGER = LoggerFactory.getLogger(BasicVaultConnection.class);

  /**
   * Construct a connection using a Vault Token
   *
   * @param id             ID for the connection
   * @param vaultToken     Token to use for authentication
   * @param vaultUrl       URL for the Vault server (https://host:port)
   * @param sslProperties  {@link SSLProperties} to use to make the connection
   * @param engineVersion  The version of the secret engine to use, defaulting to Version 2
   * @throws ConnectionException if there is a problem connecting to Vault
   */
  public BasicVaultConnection(String id, String vaultToken, String vaultUrl, SSLProperties sslProperties,
                              EngineVersion engineVersion) throws ConnectionException {
    this.id = id;
    try {
      this.vaultConfig = new VaultConfig().address(vaultUrl);
      if (engineVersion != null) {
        this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
      }
      SslConfig ssl = getVaultSSLConfig(sslProperties);
      this.vault = new Vault(this.vaultConfig.token(vaultToken).sslConfig(ssl.build()).build());
      renewable = this.vault.auth().lookupSelf().isRenewable();
      long creationTimeSec = this.vault.auth().lookupSelf().getCreationTime();
      long ttl = this.vault.auth().lookupSelf().getTTL();

      if (creationTimeSec > 0) {
        Instant creationTime = Instant.ofEpochSecond(creationTimeSec);
        if (ttl > 0) {
          this.expirationTime = creationTime.plusSeconds(ttl);
        } else {
          this.expirationTime = null;
        }
      }

      this.valid = true;
    } catch (VaultException ve) {
      LOGGER.error("Error establishing Vault connection", ve);
      throw new ConnectionException(ve.getMessage(), ve.getCause());
    }
  }
}
