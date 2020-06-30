package com.avioconsulting.mule.vault.api.client;

import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class VaultRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(VaultRequestBuilder.class);

    private HttpConstants.Protocol protocol;
    private String host;
    private int port = -1;
    private int kvVersion = 1;

    private int timeout;
    private TimeUnit timeoutUnit;

    private String secretPath;
    private String payload;
    private boolean followRedirects = false;
    private VaultConfig config;

    public VaultRequestBuilder() {
        super();
    }

    public VaultRequestBuilder protocol(HttpConstants.Protocol protocol) {
        if ("http".equals(protocol.getScheme())) {
            logger.warn("Using HTTP URI. HTTPS is recommended.");
        }
        this.protocol = protocol;
        return this;
    }

    public VaultRequestBuilder host(String host) {
        this.host = host;
        return this;
    }

    public VaultRequestBuilder port(int port) {
        if (port > -1) {
            this.port = port;
        } else if (this.protocol != null) {
            this.port = this.protocol.getDefaultPort();
        }
        return this;
    }

    public VaultRequestBuilder uri(String uri) {
        if (uri != null) {
            URI u = URI.create(uri);
            this.protocol(HttpConstants.Protocol.valueOf(u.getScheme().toUpperCase()    ));
            this.port(u.getPort());
            this.host(u.getHost());
        }
        return this;
    }

    public VaultRequestBuilder config(VaultConfig config) {
        this.config = config;
        this.timeout = config.getTimeout();
        this.timeoutUnit = config.getTimeoutUnit();
        this.kvVersion = config.getKvVersion();
        return this.uri(config.getBaseUrl()).kvVersion(config.getKvVersion());
    }

    public VaultRequestBuilder secretPath(String secretPath) {
        this.secretPath = secretPath;
        return this;
    }

    public VaultRequestBuilder payload(String payload) {
        this.payload = payload;
        return this;
    }

    public VaultRequestBuilder kvVersion(int kvVersion) {
        this.kvVersion = kvVersion;
        return this;
    }

    public VaultRequestBuilder responseTimeout(int timeout, TimeUnit timeoutUnit) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        return this;
    }

    public VaultRequestBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public VaultRequest build() {

        HttpRequestBuilder reqBuilder = HttpRequest.builder().
                uri(String.format("%s://%s:%d%s/%s", protocol.getScheme(), host, port, VaultConstants.VAULT_API_PATH, massagePath(secretPath, kvVersion)));

        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            reqBuilder.addHeader(VaultConstants.VAULT_NAMESPACE_HEADER, config.getNamespace());
        }

        if (config.isIncludeVaultRequestHeader()) {
            reqBuilder.addHeader(VaultConstants.VAULT_REQUEST_HEADER, "true");
        }

        if (payload != null && !payload.isEmpty()) {
            reqBuilder.entity(new ByteArrayHttpEntity(payload.getBytes()));
        }

        return new VaultRequest(reqBuilder, (int) timeoutUnit.toMillis(timeout), followRedirects, payload);
    }

    // helper method to update path for v1 vs v2
    private String massagePath(final String path, final int kvVersion) {
        String massagedPath = path;
        if (path.startsWith("/")) {
            massagedPath = path.substring(1);
        }

        if (kvVersion == 2) {
            String[] splitPath = massagedPath.split("/");
            StringBuilder sb = new StringBuilder();
            sb.append(splitPath[0]);
            sb.append("/data");
            for (int i = 1; i < splitPath.length; i++) {
                sb.append("/");
                sb.append(splitPath[i]);
            }
            massagedPath = sb.toString();
        }
        logger.info("messagePath() Message Path: {}", massagedPath);
        return massagedPath;
    }
}
