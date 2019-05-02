package com.avioconsulting.mule.connector.vault.provider.api.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.JKSProperties;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.PEMProperties;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A connection to Vault using TLS authentication
 *
 * @author Adam Mead
 */
public class TLSVaultConnection extends AbstractVaultConnection {

    private Logger LOGGER = LoggerFactory.getLogger(TLSVaultConnection.class);

    /**
     * Create a connection, authenticating via TLS
     * @param id                    ID for the connection
     * @param vaultUrl              URL for the Vault server (https://host:port)
     * @param jksProperties         Java Keystore properties to use for authentication
     * @param pemProperties         PEM properties to use for authentication
     * @param sslProperties         {@link SSLProperties} to use to make the connection
     * @param engineVersion         The version of the secret engine to use, defaulting to Version 2
     * @throws ConnectionException  if there is an issue connecting to Vault
     */
    public TLSVaultConnection(String id, String vaultUrl, JKSProperties jksProperties, PEMProperties pemProperties,
                              SSLProperties sslProperties, EngineVersion engineVersion)
            throws ConnectionException {

        this.id = id;
        try {

            this.vaultConfig = new VaultConfig().address(vaultUrl);

            if (engineVersion != null) {
                this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
            }

            SslConfig ssl = getVaultSSLConfig(sslProperties);

            if (jksProperties != null) {
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
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            Vault vaultDriver = new Vault(this.vaultConfig.build());
            String vaultToken = vaultDriver.auth().loginByCert().getAuthClientToken();
            this.vault = new Vault(this.vaultConfig.sslConfig(ssl.build()).token(vaultToken).build());
            this.valid = true;
        } catch (VaultException ve) {
            LOGGER.error("Error creating Vault connection",ve);
            throw new ConnectionException(ve.getMessage(), ve.getCause());
        }
    }

}