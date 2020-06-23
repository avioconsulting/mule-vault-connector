package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.vault.api.client.VaultClient;
import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.VaultConfigBuilder;
import com.avioconsulting.mule.vault.api.client.auth.AWSIAMAuthenticator;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A connection to Vault using IAM to authenticate
 *
 * @author Adam Mead
 */
public class IamVaultConnection extends AbstractVaultConnection {

    private static final Logger logger = LoggerFactory.getLogger(IamVaultConnection.class);

    public IamVaultConnection(String vaultUrl, String authMount, String role, HttpClient httpClient, String iamRequestUrl,
                              String iamRequestBody, String iamRequestHeaders, Integer responseTimeout, TimeUnit responseTimeoutUnit, boolean followRedirects) throws VaultAccessException, DefaultMuleException {
        super();
        VaultConfigBuilder builder = VaultConfig.builder().
                baseUrl(vaultUrl).
                authenticator(new AWSIAMAuthenticator(authMount, role, iamRequestUrl, iamRequestBody, iamRequestHeaders)).
                httpClient(httpClient).
                timeout(responseTimeout).
                timeoutUnit(responseTimeoutUnit).
                kvVersion(1).
                followRedirects(followRedirects);
        this.config = builder.build();
        this.vault = new VaultClient(builder.build());
        try {
            this.vault.authenticate();
        } catch (AccessException e) {
            throw new VaultAccessException(e);
        } catch (VaultException e) {
            throw new DefaultMuleException(e);
        }
    }
}
