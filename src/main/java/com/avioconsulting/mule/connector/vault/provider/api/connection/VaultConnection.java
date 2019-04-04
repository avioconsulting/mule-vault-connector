package com.avioconsulting.mule.connector.vault.provider.api.connection;

import com.bettercloud.vault.Vault;

public interface VaultConnection {

    String getId();

    Vault getVault();

    void invalidate();

    boolean isValid();

    void renewLease();
}
