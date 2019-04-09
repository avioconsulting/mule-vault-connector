package com.avioconsulting.mule.connector.vault.provider.api.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.parameter.JKSProperties;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.PEMProperties;
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

    public SSLVaultConnection(String id, String vaultUrl, JKSProperties jksProperties, PEMProperties pemProperties)
            throws ConnectionException {

        this.id = id;
        try {

            VaultConfig vaultConfig = new VaultConfig().address(vaultUrl);

            SslConfig ssl = new SslConfig();
            if (jksProperties != null) {
                if (jksProperties.getTrustStoreFile() != null && !jksProperties.getTrustStoreFile().isEmpty()) {
                    if (classpathResourceExists(jksProperties.getTrustStoreFile())) {
                        ssl = ssl.trustStoreResource(jksProperties.getTrustStoreFile());
                    } else {
                        ssl = ssl.trustStoreFile(new File(jksProperties.getTrustStoreFile()));
                    }
                }
                if (jksProperties.getKeyStoreFile() != null && jksProperties.getKeyStoreFile() != null
                        && !jksProperties.getKeyStorePassword().isEmpty()
                        && !jksProperties.getKeyStorePassword().isEmpty()) {
                    if (classpathResourceExists(jksProperties.getKeyStoreFile())) {
                        ssl = ssl.keyStoreResource(jksProperties.getKeyStoreFile(), jksProperties.getKeyStorePassword());
                    } else {
                        ssl = ssl.keyStoreFile(new File(jksProperties.getKeyStoreFile()), jksProperties.getKeyStorePassword());
                    }
                }
            } else if (pemProperties != null) {
                if (pemProperties.getPemFile() != null && !pemProperties.getPemFile().isEmpty()) {
                    if (classpathResourceExists(pemProperties.getPemFile())) {
                        ssl = ssl.pemResource(pemProperties.getPemFile());
                    } else {
                        ssl = ssl.pemFile(new File(pemProperties.getPemFile()));
                    }
                }
                if (pemProperties.getClientPemFile() != null && !pemProperties.getClientPemFile().isEmpty()) {
                    if (classpathResourceExists(pemProperties.getClientPemFile())) {
                        ssl = ssl.clientPemResource(pemProperties.getClientPemFile());
                    } else {
                        ssl = ssl.clientPemFile(new File(pemProperties.getClientPemFile()));
                    }
                }
                if (pemProperties.getClientKeyPemFile() != null && !pemProperties.getClientKeyPemFile().isEmpty()) {
                    if (classpathResourceExists(pemProperties.getClientKeyPemFile())) {
                        ssl = ssl.clientKeyPemResource(pemProperties.getClientKeyPemFile());
                    } else {
                        ssl = ssl.clientKeyPemFile(new File(pemProperties.getClientKeyPemFile()));
                    }
                }
            }
            ssl = ssl.verify(true);
            vaultConfig = vaultConfig.sslConfig(ssl.build());
            Vault vaultDriver = new Vault(vaultConfig.build());
            String vaultToken = vaultDriver.auth().loginByCert().getAuthClientToken();
            vault = new Vault(vaultConfig.sslConfig(ssl.build()).token(vaultToken).build());
        } catch (VaultException ve) {
            LOGGER.error("Error creating Vault connection",ve);
            throw new ConnectionException(ve.getMessage(), ve.getCause());
        }
    }

}