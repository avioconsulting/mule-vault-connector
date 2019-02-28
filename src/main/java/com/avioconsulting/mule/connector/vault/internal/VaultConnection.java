package com.avioconsulting.mule.connector.vault.internal;


import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class VaultConnection {

  private final String id;
  private Vault vault;
  private boolean valid = true;

  public VaultConnection(String id, String vaultToken, String vaultUrl) throws ConnectionException{
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
