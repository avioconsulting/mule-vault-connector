package com.avioconsulting.mule.vault.api.client;

import org.mule.runtime.http.api.client.HttpClient;

import java.util.concurrent.TimeUnit;

public class VaultConfig {
    private HttpClient httpClient;
    private String baseUrl;
    private Integer timeout;
    private TimeUnit timeoutUnit;
    private String token;
    private Integer kvVersion = 1;
    private Boolean followRedirects;

    public VaultConfig() {
        super();
    }

    public VaultConfig(HttpClient httpClient, String baseUrl, Integer timeout, TimeUnit timeoutUnit, String token, Integer kvVersion, Boolean followRedirects) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.token = token;
        this.kvVersion = kvVersion;
        this.followRedirects = followRedirects;
    }

    public String getApiBaseUrl() {
        if (baseUrl.endsWith("/")) {
            return baseUrl + "v1/";
        } else {
            return baseUrl + "/v1/";
        }
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeoutInMilliseconds() {
        if (timeout != null && timeoutUnit != null) {
            return (int) timeoutUnit.toMillis(timeout);
        } else {
            return 0;
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getKvVersion() {
        return kvVersion;
    }

    public void setKvVersion(Integer kvVersion) {
        this.kvVersion = kvVersion;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }
}
