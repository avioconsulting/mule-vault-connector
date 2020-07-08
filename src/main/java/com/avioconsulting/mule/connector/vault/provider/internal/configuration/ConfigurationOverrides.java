package com.avioconsulting.mule.connector.vault.provider.internal.configuration;

import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import java.util.concurrent.TimeUnit;

public final class ConfigurationOverrides {

    @Parameter
    @ConfigOverride
    EngineVersion engineVersion;

    @Parameter
    @ConfigOverride
    @Placement(tab = "Settings", order = 1)
    Integer responseTimeout;

    @Parameter
    @ConfigOverride
    @Placement(tab = "Settings", order = 2)
    TimeUnit responseTimeoutUnit;

    @Parameter
    @ConfigOverride
    @Placement(tab = "Settings", order = 3)
    boolean followRedirects;

    public EngineVersion getEngineVersion() {
        return engineVersion;
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
