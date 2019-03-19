package com.avioconsulting.mule.connector.vault.internal.connection.impl;


import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;

import java.time.Instant;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class BasicVaultConnection extends AbstractVaultConnection {

  public BasicVaultConnection(String id, String vaultToken, String vaultUrl) throws ConnectionException{
    this.id = id;
    try {
      vaultConfig = new VaultConfig().address(vaultUrl);
      vault = new Vault(vaultConfig.token(vaultToken).build());
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
