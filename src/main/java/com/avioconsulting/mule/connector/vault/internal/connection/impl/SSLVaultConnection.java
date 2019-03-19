package com.avioconsulting.mule.connector.vault.internal.connection.impl;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SSLVaultConnection extends AbstractVaultConnection {

    private Logger LOGGER = LoggerFactory.getLogger(SSLVaultConnection.class);

    public SSLVaultConnection(String id, String vaultToken, String vaultUrl, boolean verifySsl, String keyStorePath, String keyStorePassword, String trustStorePath) throws ConnectionException {
        this.id = id;
        try {

            VaultConfig vaultConfig = new VaultConfig().address(vaultUrl);

            if (vaultToken != null && !vaultToken.isEmpty()) {
                vaultConfig = vaultConfig.token(vaultToken);
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

            vault = new Vault(vaultConfig.sslConfig(ssl.build()).build());
            String token = vault.auth().loginByCert().getAuthClientToken();
            vault = new Vault(vaultConfig.token(token).build());
        } catch (VaultException ve) {
            LOGGER.error("Error creating Vault connection",ve);
            throw new ConnectionException(ve.getMessage(), ve.getCause());
        }
    }

}