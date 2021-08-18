package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.impl.BasicVaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.TLSAuthenticator;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides {@link VaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link PoolingConnectionProvider} which will pool and reuse connections.
 */
@DisplayName("TLS Connection")
@Alias("tls-connection")
public class VaultTLSConnectionProvider extends AbstractVaultConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(VaultTLSConnectionProvider.class);

    @Parameter
    protected TlsContextFactory tlsContextFactory;

    @DisplayName("Authentication Mount Path")
    @Summary("Mount path for TLS auth method. If not set, cert will be used")
    @Parameter
    @Optional(defaultValue = "cert")
    private String mount;

    @DisplayName("Certificate Role")
    @Summary("Name of certificate role to authenticate against. If not set, all will be tried.")
    @Parameter
    @Optional
    private String certificateRole;

    @Override
    public TlsContextFactory getTlsContextFactory() {
        return tlsContextFactory;
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        try {
            logger.debug("Creating TLS VaultConnection");
            VaultConfig config = VaultConfig.builder().
                    baseUrl(vaultUrl).
                    authenticator(new TLSAuthenticator(mount, certificateRole)).
                    httpClient(httpClient).
                    timeout(httpSettings.getResponseTimeout()).
                    timeoutUnit(httpSettings.getResponseTimeoutUnit()).
                    kvVersion(1).
                    followRedirects(httpSettings.isFollowRedirects()).build();
            return new BasicVaultConnection(config);
        } catch (InterruptedException | DefaultMuleException | VaultAccessException e) {
            throw new ConnectionException(e);
        }
    }
}
