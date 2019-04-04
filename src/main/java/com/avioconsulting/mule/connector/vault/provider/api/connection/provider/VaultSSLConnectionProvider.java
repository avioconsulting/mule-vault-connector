package com.avioconsulting.mule.connector.vault.provider.api.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.connection.impl.SSLVaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.JKSProperties;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.PEMProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

@DisplayName("SSL Connection")
@Alias("ssl-connection")
public class VaultSSLConnectionProvider implements PoolingConnectionProvider<VaultConnection> {

    private final Logger LOGGER = LoggerFactory.getLogger(VaultSSLConnectionProvider.class);

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

    @DisplayName("JKS Properties")
    @Parameter
    @Optional
    private JKSProperties jksProperties;

    @DisplayName("PEM Properties")
    @Parameter
    @Optional
    private PEMProperties pemProperties;


    @Override
    public VaultConnection connect() throws ConnectionException {
        int connectionNumber = (new Random()).nextInt();
        return new SSLVaultConnection("ssl_conn_" + connectionNumber, vaultUrl, jksProperties, pemProperties);
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
