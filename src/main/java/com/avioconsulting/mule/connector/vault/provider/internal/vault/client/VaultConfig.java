package com.avioconsulting.mule.connector.vault.provider.internal.vault.client;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.VaultAuthenticator;
import org.mule.runtime.http.api.client.HttpClient;

import java.util.concurrent.TimeUnit;

public class VaultConfig {

    private HttpClient httpClient;
    private String baseUrl;
    private Integer timeout;
    private TimeUnit timeoutUnit;
    private Integer kvVersion = 1;
    private boolean followRedirects = false;
    private String namespace;
    private boolean includeVaultRequestHeader = true;
    private VaultAuthenticator authenticator;

    public VaultConfig() {
        super();
    }

    public VaultConfig(HttpClient httpClient, VaultAuthenticator authenticator, String baseUrl, Integer timeout, TimeUnit timeoutUnit, Integer kvVersion, Boolean followRedirects) {
        this.httpClient = httpClient;
        this.authenticator = authenticator;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.kvVersion = kvVersion;
        this.followRedirects = followRedirects;
    }

    public VaultConfig(HttpClient httpClient, VaultAuthenticator authenticator, String baseUrl, Integer timeout, TimeUnit timeoutUnit, Integer kvVersion, Boolean followRedirects, String namespace, boolean includeVaultRequestHeader) {
        this.httpClient = httpClient;
        this.authenticator = authenticator;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.kvVersion = kvVersion;
        this.followRedirects = followRedirects;
        this.namespace = namespace;
        this.includeVaultRequestHeader = includeVaultRequestHeader;
    }

    public static VaultConfigBuilder builder() {
        return new VaultConfigBuilder();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getTimeoutInMilliseconds() {
        if (timeout != null && timeoutUnit != null) {
            return (int) timeoutUnit.toMillis(timeout);
        } else {
            return 0;
        }
    }

    public Integer getKvVersion() {
        return kvVersion;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean isIncludeVaultRequestHeader() {
        return includeVaultRequestHeader;
    }

    public VaultAuthenticator getAuthenticator() {
        return this.authenticator;
    }
}
