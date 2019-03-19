package com.avioconsulting.mule.connector.vault.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.internal.connection.impl.SSLVaultConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("SSL Connection")
@Alias("ssl-connection")
public class VaultSSLConnectionProvider implements PoolingConnectionProvider<VaultConnection> {

    private final Logger LOGGER = LoggerFactory.getLogger(VaultSSLConnectionProvider.class);

    @DisplayName("Vault Token")
    @Optional
    @Parameter
    private String vaultToken;

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

    @DisplayName("Verify SSL")
    @Parameter
    private boolean verifySSL = true;

    @DisplayName("KeyStore Path")
    @Summary("Path to the KeyStore if using Vault's TLS Certificate auth backend for client side authentication. The KeyStore password must also be provided.")
    @Optional
    @Parameter
    private String keyStorePath;

    @DisplayName("KeyStore Password")
    @Optional
    @Password
    @Parameter
    private String keyStorePassword;

    @DisplayName("TrustStore Path")
    @Optional
    @Parameter
    private String trustStorePath;

    @Override
    public VaultConnection connect() throws ConnectionException {
        return new SSLVaultConnection(vaultToken + ":" + vaultUrl, vaultToken, vaultUrl, verifySSL, keyStorePath, keyStorePassword, trustStorePath);
    }

    @Override
    public void disconnect(VaultConnection connection) {
        try {
            connection.invalidate();
        } catch (Exception e) {
            LOGGER.error("Error while disconnecting [" + connection.getId() + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public ConnectionValidationResult validate(VaultConnection connection) {
        if (connection.isValid()) {
            return ConnectionValidationResult.success();
        } else {
            return ConnectionValidationResult.failure("Connection Invalid", null);
        }

    }

}
