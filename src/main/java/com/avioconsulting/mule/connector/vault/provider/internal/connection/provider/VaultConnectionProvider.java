package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.impl.BasicVaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.TokenAuthenticator;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides {@link BasicVaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link PoolingConnectionProvider} which will pool and reuse connections.
 */
@DisplayName("Basic Connection")
@Alias("basic-connection")
public class VaultConnectionProvider extends AbstractVaultConnectionProvider {

  private static final Logger logger = LoggerFactory.getLogger(VaultConnectionProvider.class);

  @DisplayName("Vault Token")
  @Parameter
  private String vaultToken;

  @Parameter
  @Placement(tab = "Security")
  @Optional
  protected TlsContextFactory tlsContextFactory;

  @Override
  public TlsContextFactory getTlsContextFactory() {
    return tlsContextFactory;
  }

  @Override
  public VaultConnection connect() throws ConnectionException {
    try {
      logger.debug("Creating Token VaultConnection");
      VaultConfig config = VaultConfig.builder().
              authenticator(new TokenAuthenticator(vaultToken)).
              httpClient(httpClient).
              baseUrl(vaultUrl).
              timeout(httpSettings.getResponseTimeout()).
              timeoutUnit(httpSettings.getResponseTimeoutUnit()).
              followRedirects(httpSettings.isFollowRedirects()).
              build();
      return new BasicVaultConnection(config);
    } catch (InterruptedException | DefaultMuleException e) {
      throw new ConnectionException(e);
    }
  }


}
