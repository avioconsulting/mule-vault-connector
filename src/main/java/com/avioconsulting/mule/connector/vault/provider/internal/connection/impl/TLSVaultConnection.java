package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A connection to Vault using TLS authentication
 *
 * @author Adam Mead
 */
public class TLSVaultConnection extends AbstractVaultConnection {

    private static final Logger logger = LoggerFactory.getLogger(TLSVaultConnection.class);

    private final String authMount;
    private String certificateRole;

    public TLSVaultConnection(String vaultUrl, String authMount, String certRole, HttpClient httpClient, EngineVersion engineVersion, Integer requestTimeout, Boolean followRedirects) throws VaultAccessException, DefaultMuleException{
        super();
        this.client = httpClient;
        this.authMount = authMount;
        this.certificateRole = certRole;
        this.vaultUrl = vaultUrl;
        this.requestTimeout = requestTimeout;
        this.followRedirects = followRedirects;
        this.engineVersion = engineVersion;

        this.token = authenticate();
        this.vConfig = new VaultConfig(this.client, this.vaultUrl, requestTimeout, this.token, this.engineVersion.getEngineVersionNumber(), followRedirects);
    }

    @Override
    public boolean isValid() {
        if (this.token == null || this.token.isEmpty()) {
            try {
                this.token = authenticate();
            } catch (DefaultMuleException e) {
                logger.error("Error Authenticating", e);
            }
        }
        return this.token != null && !this.token.isEmpty();
    }

    private String authenticate() throws VaultAccessException, DefaultMuleException {
        String token = null;
        String mount = "cert";

        if (authMount != null && !authMount.isEmpty()) {
            mount = authMount;
        }

        HttpRequestBuilder builder = HttpRequest.builder().
                uri(this.vaultUrl + "/v1/auth/" + mount + "/login").
                method(HttpConstants.Method.POST);

        if (this.certificateRole != null && !this.certificateRole.isEmpty()) {
            JsonObject json = new JsonObject();
            json.addProperty("name", this.certificateRole);
            builder.entity(new ByteArrayHttpEntity(json.toString().getBytes()));
        }


        CompletableFuture<HttpResponse> completable = client.sendAsync(builder.build(), this.requestTimeout, this.followRedirects, null);

        try {
            HttpResponse response = completable.get();

            if (response.getStatusCode() == 200) {
                JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
                JsonElement authData = elem.getAsJsonObject().get("auth");
                if (authData != null) {
                    JsonElement clientToken = authData.getAsJsonObject().get("client_token");
                    token = clientToken.getAsString();
                }
            } else if (response.getStatusCode() == 403 || response.getStatusCode() == 404){
                throw new VaultAccessException(new Exception("Access Error received from Vault: " + response.getStatusCode()));
            } else {
                throw new DefaultMuleException(new Exception("Unknown error received from Vault: " + response.getStatusCode()));
            }

        } catch (InterruptedException | ExecutionException e ) {
            logger.error("Exception encountered while authenticating", e);
            throw new DefaultMuleException(e);
        }

        return token;
    }

}