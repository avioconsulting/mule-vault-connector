package com.avioconsulting.mule.connector.vault.provider.api.connection.impl;


import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.File;
import java.time.Instant;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class BasicVaultConnection extends AbstractVaultConnection {

  public BasicVaultConnection(String id, String vaultToken, String vaultUrl, String pemFile, String trustStoreFile) throws ConnectionException{
    this.id = id;
    try {
      vaultConfig = new VaultConfig().address(vaultUrl);
      SslConfig ssl = new SslConfig();
      if (pemFile != null && !pemFile.isEmpty()) {
        if (classpathResourceExists(pemFile)) {
          ssl = ssl.pemResource(pemFile);
        } else {
          ssl = ssl.pemFile(new File(pemFile));
        }
        ssl = ssl.verify(true);
      } else if (trustStoreFile != null && !trustStoreFile.isEmpty()) {
        if (classpathResourceExists(trustStoreFile)) {
          ssl = ssl.trustStoreResource(trustStoreFile);
        } else {
          ssl = ssl.trustStoreFile(new File(trustStoreFile));
        }
        ssl = ssl.verify(true);
      }
      vault = new Vault(vaultConfig.token(vaultToken).sslConfig(ssl.build()).build());
      renewable = vault.auth().lookupSelf().isRenewable();
      long creationTimeSec = vault.auth().lookupSelf().getCreationTime();
      long ttl = vault.auth().lookupSelf().getTTL();

      if (creationTimeSec > 0) {
        Instant creationTime = Instant.ofEpochSecond(creationTimeSec);
        if (ttl > 0) {
          expirationTime = creationTime.plusSeconds(ttl);
        } else {
          expirationTime = null;
        }
      }

    } catch (VaultException ve) {
      throw new ConnectionException(ve.getMessage(), ve.getCause());
    }
  }
}
