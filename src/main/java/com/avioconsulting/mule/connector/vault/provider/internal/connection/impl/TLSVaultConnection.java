package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.vault.api.client.VaultClient;
import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.VaultConfigBuilder;
import com.avioconsulting.mule.vault.api.client.auth.TLSAuthenticator;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A connection to Vault using TLS authentication
 *
 * @author Adam Mead
 */
public class TLSVaultConnection extends AbstractVaultConnection {

    private static final Logger logger = LoggerFactory.getLogger(TLSVaultConnection.class);

    public TLSVaultConnection(String vaultUrl, String authMount, String certRole, HttpClient httpClient, Integer responseTimeout, TimeUnit responseTimeoutUnit, Boolean followRedirects) throws VaultAccessException, DefaultMuleException{
        super();
        VaultConfigBuilder builder = VaultConfig.builder().
                baseUrl(vaultUrl).
                authenticator(new TLSAuthenticator(authMount, certRole)).
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