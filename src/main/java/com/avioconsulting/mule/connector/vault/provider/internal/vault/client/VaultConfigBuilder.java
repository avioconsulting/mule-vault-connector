package com.avioconsulting.mule.connector.vault.provider.internal.vault.client;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.VaultAuthenticator;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;

import java.util.concurrent.TimeUnit;

public class VaultConfigBuilder {
    private HttpClient httpClient;
    private HttpService httpService;
    private VaultAuthenticator authenticator;
    private String baseUrl;
    private Integer timeout;
    private TimeUnit timeoutUnit;
    private Integer kvVersion = 1;
    private boolean followRedirects = false;
    private String namespace;
    private boolean includeVaultRequestHeader = true;

    private TlsContextFactory tlsContextFactory;

    public VaultConfigBuilder() {super();}

    public VaultConfig build() {
        return new VaultConfig(httpClient, authenticator, baseUrl, timeout, timeoutUnit, kvVersion, followRedirects, namespace, includeVaultRequestHeader,
              httpService);
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

    public VaultConfigBuilder httpService(HttpService httpService) {
        this.httpService = httpService;
        return this;
    }
}
