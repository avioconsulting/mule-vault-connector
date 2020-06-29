package com.avioconsulting.mule.connector.vault.provider.internal.configuration;

import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultEc2ConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultIamConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.provider.VaultTLSConnectionProvider;
import com.avioconsulting.mule.connector.vault.provider.internal.operation.VaultOperations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

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
        return responseTimeout;
    }

    public TimeUnit getResponseTimeoutUnit() {
        return responseTimeoutUnit;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }
}
