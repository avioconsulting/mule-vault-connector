package com.avioconsulting.mule.connector.vault.internal.connection;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;

public interface VaultConnection {

    String getId();

    Vault getVault();

    void invalidate();

    boolean isValid();
}
