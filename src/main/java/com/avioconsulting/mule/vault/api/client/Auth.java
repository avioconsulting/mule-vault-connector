package com.avioconsulting.mule.vault.api.client;

import org.mule.runtime.http.api.client.HttpClient;

public class Auth {

    private VaultConfig config;

    public Auth(VaultConfig config) {
        this.config = config;
    }

}
