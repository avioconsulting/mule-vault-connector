package com.avioconsulting.mule.connector.vault.provider.api;

import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultEc2ConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultIamConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultTLSConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.operation.VaultOperations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

/**
 * This class represents the extension configuration.
 */
@Operations(VaultOperations.class)
@ConnectionProviders({VaultConnectionProvider.class, VaultTLSConnectionProvider.class, VaultIamConnectionProvider.class, VaultEc2ConnectionProvider.class})
public class VaultConfiguration {


}
