package com.avioconsulting.mule.connector.vault.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.internal.connection.VaultConnection;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.File;

public class VaultSSLConnection implements VaultConnection {

    private final String id;
    private Vault vault;
    private boolean valid = true;

    public VaultSSLConnection(String id, String vaultToken, String vaultUrl, boolean verifySsl, String keyStorePath, String keyStorePassword, String trustStorePath) throws ConnectionException {
        this.id = id;
        try {

            VaultConfig vaultConfig = new VaultConfig().address(vaultUrl);

            if (vaultToken != null && !vaultToken.isEmpty()) {

            }

            SslConfig ssl = new SslConfig();
            if (keyStorePath != null && keyStorePassword != null && !keyStorePath.isEmpty() && !keyStorePassword.isEmpty()) {
                File keyStoreFile = new File(keyStorePath);
                if (keyStoreFile.exists() && keyStoreFile.isFile()) {
                    ssl = ssl.keyStoreFile(keyStoreFile,keyStorePassword);
                } else {
                    ssl = ssl.keyStoreResource(keyStorePath,keyStorePassword);
                }

            }
            if (trustStorePath != null && trustStorePath.isEmpty()) {
                File trustStoreFile = new File(trustStorePath);
                if (trustStoreFile.exists() && trustStoreFile.isFile()) {
                    ssl = ssl.trustStoreFile(trustStoreFile);
                } else {
                    ssl = ssl.trustStoreResource(trustStorePath);
                }
            }
            ssl = ssl.verify(verifySsl);

            vault = new Vault(new VaultConfig().address(vaultUrl).token(vaultToken).sslConfig(ssl.build()).build());
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