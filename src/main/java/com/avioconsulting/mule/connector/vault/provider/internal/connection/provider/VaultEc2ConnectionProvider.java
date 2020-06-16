package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy.VaultProxyConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.impl.Ec2VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
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
 * This class provides {@link Ec2VaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link PoolingConnectionProvider} which will pool and reuse connections.
 */
@DisplayName("EC2 Connection")
@Alias("ec2-connection")
public class VaultEc2ConnectionProvider implements CachedConnectionProvider<VaultConnection>, Startable, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(VaultEc2ConnectionProvider.class);

    @Inject
    private HttpService httpService;
    private HttpClient httpClient;

    @RefName
    private String configName;

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

    @DisplayName("Secrets Engine Version")
    @Parameter
    @Optional
    private EngineVersion engineVersion;

    @DisplayName("Vault AWS Authentication Mount")
    @Summary("Mount point for AWS Authentication in Vault")
    @Parameter
    private String awsAuthMount;

    @DisplayName("Vault Role")
    @Parameter
    private String vaultRole;

    @DisplayName("PKCS7 Signature")
    @Summary("PKCS7 signature of the identity document with all \\n characters removed.")
    @Optional
    @Parameter
    private String pkcs7;

    @DisplayName("Identity Document")
    @Summary("Base64 encoded EC2 instance identity document.")
    @Optional
    @Parameter
    private String identity;

    @DisplayName("Identity Document Signature")
    @Summary("Base64 encoded SHA256 RSA signature of the instance identity document")
    @Optional
    @Parameter
    private String signature;

    @DisplayName("Nonce")
    @Summary("Nonce to be used for subsequent login requests. If not provided, reauthentication will not be allowed.")
    @Optional
    @Parameter
    private String nonce;

    @DisplayName("Use Instance Metadata")
    @Summary("Retrieve Instance metadata")
    @Parameter
    private boolean useInstanceMetadata = false;

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

    /**
     * Constructs an {@link Ec2VaultConnection}. When useInstanceMetadata is true, the PKCS7 value is looked up from
     * the AWS Metadata Service
     *
     * @return an {@link Ec2VaultConnection}
     * @throws ConnectionException
     */
    @Override
    public VaultConnection connect() throws ConnectionException {
        if (engineVersion == null) {
            engineVersion = EngineVersion.v1;
        }
        try {
            return new Ec2VaultConnection(vaultUrl, awsAuthMount, vaultRole, httpClient, engineVersion, pkcs7, nonce, identity, signature, useInstanceMetadata, responseTimeout, responseTimeoutUnit, followRedirects);
        } catch (DefaultMuleException e) {
            throw new ConnectionException(e);
        }
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
