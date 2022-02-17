package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.impl.BasicVaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.AWSIAMAuthenticator;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
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
 * This class provides {@link VaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link CachedConnectionProvider} which will cache and reuse connections.
 */
@DisplayName("IAM Connection")
@Alias("iam-connection")
public class VaultIamConnectionProvider extends AbstractVaultConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(VaultIamConnectionProvider.class);

    @Parameter
    @Placement(tab = "Security")
    @Optional
    protected TlsContextFactory tlsContextFactory;

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

    @DisplayName("IAM Server ID")
    @Parameter
    @Optional
    private String iamServerId;

    @DisplayName("IAM Access Key")
    @Parameter
    private String iamAccessKey;

    @DisplayName("IAM Secret Key")
    @Parameter
    private String iamSecretKey;

    @Override
    public TlsContextFactory getTlsContextFactory() {
        return tlsContextFactory;
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        try {
            logger.debug("Creating AWS IAM VaultConnection");
            VaultConfig config = VaultConfig.builder().
                    baseUrl(vaultUrl).
                    authenticator(new AWSIAMAuthenticator(awsAuthMount, vaultRole, iamRequestUrl, iamRequestBody, iamServerId, iamAccessKey, iamSecretKey)).
                    httpClient(httpClient).
                    timeout(httpSettings.getResponseTimeout()).
                    timeoutUnit(httpSettings.getResponseTimeoutUnit()).
                    kvVersion(1).
                    followRedirects(httpSettings.isFollowRedirects()).
                    build();
            return new BasicVaultConnection(config);
        } catch (InterruptedException | VaultAccessException | DefaultMuleException e) {
            throw new ConnectionException(e);
        }

    }
}
