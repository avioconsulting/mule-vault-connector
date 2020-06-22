package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy.VaultProxyConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.impl.IamVaultConnection;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * This class provides {@link IamVaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link CachedConnectionProvider} which will cache and reuse connections.
 */
@DisplayName("IAM Connection")
@Alias("iam-connection")
public class VaultIamConnectionProvider implements CachedConnectionProvider<VaultConnection>, Startable, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(VaultIamConnectionProvider.class);

    @Inject
    private HttpService httpService;
    private HttpClient httpClient;

    @RefName
    private String configName;

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

    @DisplayName("Vault AWS Authentication Mount")
    @Summary("Mount point for AWS Authentication in Vault")
    @Parameter
    private String awsAuthMount;

    @DisplayName("Vault Role")
    @Summary("Name of the role against which the login is being attempted.")
    @Optional
    @Parameter
    private String vaultRole;

    @DisplayName("IAM Request URL")
    @Summary("Base64 encoded used in the signed request. Most likely aHR0cHM6Ly9zdHMuYW1hem9uYXdzLmNvbS8=")
    @Parameter
    private String iamRequestUrl;

    @DisplayName("IAM Request Body")
    @Summary("Base64 encoded body of the signed request. Most likely QWN0aW9uPUdldENhbGxlcklkZW50aXR5JlZlcnNpb249MjAxMS0wNi0xNQ==")
    @Parameter
    private String iamRequestBody;

    @DisplayName("IAM Request Headers")
    @Parameter
    private String iamRequestHeaders;

    @Parameter
    @Placement(tab = "Security")
    @Optional
    private TlsContextFactory tlsContextFactory;

    @DisplayName("Response Timeout")
    @Summary("Maximum time to wait for a response")
    @Parameter
    @Placement(tab = "Settings", order = 1)
    @Optional(defaultValue = "5")
    private Integer responseTimeout;

    @DisplayName("Response Timeout Unit")
    @Summary("Time Unit to use for response timeout value")
    @Parameter
    @Placement(tab = "Settings", order = 2)
    @Optional(defaultValue = "SECONDS")
    private TimeUnit responseTimeoutUnit;

    @DisplayName("Follow Redirects")
    @Summary("Specifies whether to follow redirects or not")
    @Parameter
    @Placement(tab = "Settings", order = 3)
    @Optional(defaultValue = "false")
    private boolean followRedirects;

    @Parameter
    @Optional
    @Placement(tab = "Proxy")
    private VaultProxyConfig proxyConfig;

    @Override
    public VaultConnection connect() throws ConnectionException {
        try {
            return new IamVaultConnection(vaultUrl, awsAuthMount, vaultRole, httpClient, iamRequestUrl, iamRequestBody, iamRequestHeaders, responseTimeout, responseTimeoutUnit, followRedirects);
        } catch (VaultAccessException | DefaultMuleException e) {
            throw new ConnectionException(e);
        }

    }

    @Override
    public void disconnect(VaultConnection connection) {
        connection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(VaultConnection connection) {
        try {
            if (connection.isValid()) {
                return ConnectionValidationResult.success();
            } else {
                return ConnectionValidationResult.failure("Connection Invalid", null);
            }
        } catch (DefaultMuleException e) {
            return ConnectionValidationResult.failure("Connection Invalid", e);
        }
    }

    @Override
    public void start() throws MuleException {
        if (tlsContextFactory instanceof Initialisable) {
            ((Initialisable) tlsContextFactory).initialise();
        }
        HttpClientConfiguration.Builder builder = new HttpClientConfiguration.Builder();
        if (tlsContextFactory != null) {
            if (tlsContextFactory.getTrustStoreConfiguration() != null) {
                logger.info("Vault TLS Trust Store Path: " + tlsContextFactory.getTrustStoreConfiguration().getPath());
            }
            if (tlsContextFactory.getKeyStoreConfiguration() != null) {
                logger.info("Vault TLS Key Store Path: " + tlsContextFactory.getKeyStoreConfiguration().getPath());
            }
            builder.setTlsContextFactory(tlsContextFactory);
        }
        if (proxyConfig != null) {
            builder.setProxyConfig(proxyConfig);
        }
        httpClient = httpService.getClientFactory().create(builder.setName(configName).build());
        httpClient.start();
    }

    @Override
    public void stop() throws MuleException {
        httpClient.stop();
    }
}
