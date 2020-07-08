package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.concurrent.TimeUnit;

public class HttpSettings {

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
