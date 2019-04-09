package com.avioconsulting.mule.connector.vault.provider.api;

import com.avioconsulting.mule.connector.vault.provider.api.connection.provider.VaultConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.api.connection.provider.VaultEc2ConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.api.connection.provider.VaultIamConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.api.connection.provider.VaultSSLConnectionProvider;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(VaultOperations.class)
@ConnectionProviders({VaultConnectionProvider.class, VaultSSLConnectionProvider.class, VaultIamConnectionProvider.class, VaultEc2ConnectionProvider.class})
public class VaultConfiguration {

  @Parameter
  private String configId;

  public String getConfigId(){
    return configId;
  }
}
