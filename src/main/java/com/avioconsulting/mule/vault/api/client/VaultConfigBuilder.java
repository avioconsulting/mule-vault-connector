package com.avioconsulting.mule.vault.api.client;

import com.avioconsulting.mule.vault.api.client.auth.VaultAuthenticator;
import org.mule.runtime.http.api.client.HttpClient;

import java.util.concurrent.TimeUnit;

public class VaultConfigBuilder {
    private HttpClient httpClient;
    private VaultAuthenticator authenticator;
    private String baseUrl;
    private Integer timeout;
    private TimeUnit timeoutUnit;
    private String token;
    private Integer kvVersion = 1;
    private boolean followRedirects = false;
    private String namespace;
    private boolean includeVaultRequestHeader = true;

    public VaultConfigBuilder() {super();}

    public VaultConfig build() {
        return new VaultConfig(httpClient, authenticator, baseUrl, timeout, timeoutUnit, token, kvVersion, followRedirects, namespace, includeVaultRequestHeader);
    }

    public VaultConfigBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public VaultConfigBuilder authenticator(VaultAuthenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    public VaultConfigBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public VaultConfigBuilder timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public VaultConfigBuilder timeoutUnit(TimeUnit timeoutUnit) {
        this.timeoutUnit = timeoutUnit;
        return this;
    }

    public VaultConfigBuilder token(String token) {
        this.token = token;
        return this;
    }

    public VaultConfigBuilder kvVersion(Integer kvVersion) {
        this.kvVersion = kvVersion;
        return this;
    }

    public VaultConfigBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public VaultConfigBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }
    public VaultConfigBuilder includeVaultRequestHeader(boolean includeVaultRequestHeader) {
        this.includeVaultRequestHeader = includeVaultRequestHeader;
        return this;
    }
}
