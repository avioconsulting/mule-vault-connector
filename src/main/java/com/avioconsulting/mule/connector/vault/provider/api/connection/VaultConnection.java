package com.avioconsulting.mule.connector.vault.provider.api.connection;

import com.bettercloud.vault.Vault;

/**
 * Vault Connection Interface used for all Vault Connections
 *
 * @author Adam Mead
 */
public interface VaultConnection {

    String getId();

    Vault getVault();

    void invalidate();

    boolean isValid();

    void renewLease();
}
