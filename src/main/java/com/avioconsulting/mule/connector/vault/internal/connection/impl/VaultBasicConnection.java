package com.avioconsulting.mule.connector.vault.internal.connection.impl;


import com.avioconsulting.mule.connector.vault.internal.connection.VaultConnection;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class VaultBasicConnection implements VaultConnection {

  private final String id;
  private Vault vault;
  private boolean valid = true;

  public VaultBasicConnection(String id, String vaultToken, String vaultUrl) throws ConnectionException{
    this.id = id;
    try {
      vault = new Vault(new VaultConfig().address(vaultUrl).token(vaultToken).build());
    } catch (VaultException ve) {
      throw new ConnectionException(ve.getMessage(), ve.getCause());
    }
  }

  public String getId() {
    return id;
  }

  public Vault getVault() {
    return vault;
  }

  public void invalidate() {
    vault = null;
    valid = false;
  }

  public boolean isValid() {
    return valid;
  }
}
