package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.impl.BasicVaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.AWSEC2Authenticator;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides {@link BasicVaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link PoolingConnectionProvider} which will pool and reuse connections.
 */
@DisplayName("EC2 Connection")
@Alias("ec2-connection")
public class VaultEc2ConnectionProvider extends AbstractVaultConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(VaultEc2ConnectionProvider.class);

    @Parameter
    @Placement(tab = "Security")
    @Optional
    protected TlsContextFactory tlsContextFactory;

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

    @Override
    public TlsContextFactory getTlsContextFactory() {
        return tlsContextFactory;
    }

    /**
     * Constructs an {@link VaultConnection}. When useInstanceMetadata is true, the PKCS7 value is looked up from
     * the AWS Metadata Service
     *
     * @return an {@link VaultConnection}
     * @throws ConnectionException
     */
    @Override
    public VaultConnection connect() throws ConnectionException {
        try {
            logger.debug("Creating AWS EC2 VaultConnection");
            VaultConfig config = VaultConfig.builder().
                    baseUrl(vaultUrl).
                    authenticator(new AWSEC2Authenticator(awsAuthMount, vaultRole, pkcs7, nonce, identity, signature, useInstanceMetadata)).
                    httpClient(httpClient).
                    timeout(httpSettings.getResponseTimeout()).
                    timeoutUnit(httpSettings.getResponseTimeoutUnit()).
                    kvVersion(1).
                    followRedirects(httpSettings.isFollowRedirects()).
                    build();

            return new BasicVaultConnection(config);
        } catch (InterruptedException | DefaultMuleException e) {
            throw new ConnectionException(e);
        }
    }
}
