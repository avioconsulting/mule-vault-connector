package com.avioconsulting.mule.connector.vault.provider.internal.configuration;

import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.HttpSettings;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultEc2ConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultIamConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultTLSConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.operation.VaultOperations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import java.util.concurrent.TimeUnit;

/**
 * This class represents the extension configuration.
 */
@Operations(VaultOperations.class)
@ConnectionProviders({VaultConnectionProvider.class, VaultTLSConnectionProvider.class, VaultIamConnectionProvider.class, VaultEc2ConnectionProvider.class})
public class VaultConfiguration {
    @DisplayName("Secrets Engine Version")
    @Parameter
    @Optional(defaultValue = "V1")
    private EngineVersion engineVersion;

    @DisplayName("Include Vault Request Header")
    @Parameter
    @Placement(tab = Placement.ADVANCED_TAB, order = 1)
    @Optional(defaultValue = "true")
    private boolean includeVaultRequestHeader;

    @Parameter
    @Placement(tab = Placement.ADVANCED_TAB, order = 2)
    @Optional
    private String vaultNamespace;

    @ParameterGroup(name = "Settings")
    private HttpSettings httpSettings;

    public EngineVersion getEngineVersion() {
        return engineVersion;
    }

    public boolean isIncludeVaultRequestHeader() {
        return includeVaultRequestHeader;
    }

    public String getVaultNamespace() {
        return vaultNamespace;
    }

    public Integer getResponseTimeout() {
        return httpSettings.getResponseTimeout();
    }

    public TimeUnit getResponseTimeoutUnit() {
        return httpSettings.getResponseTimeoutUnit();
    }

    public boolean isFollowRedirects() {
        return httpSettings.isFollowRedirects();
    }
}
