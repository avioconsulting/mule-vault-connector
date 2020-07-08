package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.api.parameter.HttpSettings;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy.VaultProxyConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public abstract class AbstractVaultConnectionProvider implements CachedConnectionProvider<VaultConnection>, Startable, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVaultConnectionProvider.class);

    @Inject
    protected HttpService httpService;
    protected HttpClient httpClient;

    @RefName
    protected String configName;

    @DisplayName("Vault URL")
    @Parameter
    protected String vaultUrl;

    @ParameterGroup(name = "Settings")
    protected HttpSettings httpSettings;

    @Parameter
    @Optional
    @Placement(tab = "Proxy")
    protected VaultProxyConfig proxyConfig;

    public abstract TlsContextFactory getTlsContextFactory();

    @Override
    public VaultConnection connect() throws ConnectionException {
        return null;
    }

    @Override
    public void disconnect(VaultConnection connection) {
        connection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(VaultConnection connection) {
        if (connection.isValid()) {
            return ConnectionValidationResult.success();
        } else {
            return ConnectionValidationResult.failure("Connection Invalid", null);
        }
    }

    @Override
    public void start() throws MuleException {
        if (getTlsContextFactory() instanceof Initialisable) {
            ((Initialisable) getTlsContextFactory()).initialise();
        }
        HttpClientConfiguration.Builder builder = new HttpClientConfiguration.Builder();
        if (getTlsContextFactory() != null) {
            if (getTlsContextFactory().getTrustStoreConfiguration() != null) {
                logger.info("Vault TLS Trust Store Path: {}", getTlsContextFactory().getTrustStoreConfiguration().getPath());
            }
            if (getTlsContextFactory().getKeyStoreConfiguration() != null) {
                logger.info("Vault TLS Key Store Path: {}", getTlsContextFactory().getKeyStoreConfiguration().getPath());
            }
            builder.setTlsContextFactory(getTlsContextFactory());
        }
        if (proxyConfig != null) {
            builder.setProxyConfig(proxyConfig);
        }
        httpClient = httpService.getClientFactory().create(builder.setName(configName).build());
        httpClient.start();
    }

    @Override
    public void stop() {
        httpClient.stop();
    }
}
