package com.avioconsulting.mule.vault.api.client;

import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

public class VaultRequest {
    HttpRequestBuilder requestBuilder;
    int responseTimeout;
    boolean followRedirects;
    String payload;

    protected VaultRequest(HttpRequestBuilder requestBuilder, int responseTimeout, boolean followRedirects, String payload) {
        this.requestBuilder = requestBuilder;
        this.responseTimeout = responseTimeout;
        this.followRedirects = followRedirects;
        this.payload = payload;
    }

    public HttpRequestBuilder getHttpRequestBuilder() {
        return this.requestBuilder;
    }

    public int getResponseTimeout() {
        return this.responseTimeout;
    }

    public boolean isFollowRedirects() {
        return this.followRedirects;
    }

    public String getPayload() {
        return this.payload;
    }
}
