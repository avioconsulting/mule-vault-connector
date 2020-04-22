package com.avioconsulting.mule.vault.api.client;

import org.mule.runtime.http.api.client.HttpClient;

public class VaultClient {

    private VaultConfig vaultConfig;

    public VaultClient(HttpClient httpClient, String baseUrl, Integer timeout, String token, Integer kvVersion) {
        super();
        vaultConfig = new VaultConfig();
        vaultConfig.setHttpClient(httpClient);
        vaultConfig.setBaseUrl(baseUrl);
        if (timeout != null) {
            vaultConfig.setTimeout(timeout);
        }
        if (kvVersion != null) {
            vaultConfig.setKvVersion(kvVersion);
        }
        if (token != null) {
            vaultConfig.setToken(token);
        }
    }

    public VaultClient(VaultConfig vaultConfig) {
        super();
        this.vaultConfig = vaultConfig;
    }

    public void setToken(String token) {
        this.vaultConfig.setToken(token);
    }

    public String getToken() {
        return this.vaultConfig.getToken();
    }

    public Auth auth() {
        return new Auth(vaultConfig);
    }

    public Logical logical() {
        return new Logical(vaultConfig);
    }
}
