package com.avioconsulting.mule.connector.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.impl.BasicVaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * This class provides {@link BasicVaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link PoolingConnectionProvider} which will pool and reuse connections.
 */
@DisplayName("Basic Connection")
@Alias("basic-connection")
public class VaultConnectionProvider implements CachedConnectionProvider<VaultConnection>, Startable, Stoppable {

  private static final Logger logger = LoggerFactory.getLogger(VaultConnectionProvider.class);

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

  @DisplayName("Vault Token")
  @Parameter
  private String vaultToken;

  @Parameter
  @Optional
  private TlsContextFactory tlsContextFactory;

  @Override
  public VaultConnection connect() throws ConnectionException {
    if (engineVersion == null) {
      engineVersion = EngineVersion.v2;
    }
    return new BasicVaultConnection(vaultToken, vaultUrl, httpClient, engineVersion);
  }

  @Override
  public void disconnect(VaultConnection connection) {
    try {
      connection.invalidate();
    } catch (Exception e) {
      logger.error("Error while disconnecting [" + connection.getId() + "]: " + e.getMessage(), e);
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
    httpClient = httpService.getClientFactory().create(builder.setName(configName).build());
    httpClient.start();
  }

  @Override
  public void stop() throws MuleException {
    httpClient.stop();
  }
}
