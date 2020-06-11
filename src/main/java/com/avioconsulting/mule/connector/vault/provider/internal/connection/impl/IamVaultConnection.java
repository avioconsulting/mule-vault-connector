package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
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
 * A connection to Vault using IAM to authenticate
 *
 * @author Adam Mead
 */
public class IamVaultConnection extends AbstractVaultConnection {

    private static final Logger logger = LoggerFactory.getLogger(IamVaultConnection.class);

    private String authMount;
    private String role;
    private String iamRequestUrl;
    private String iamRequestBody;
    private String iamRequestHeaders;

    public IamVaultConnection(String vaultUrl, String authMount, String role, HttpClient httpClient, EngineVersion engineVersion, String iamRequestUrl,
                              String iamRequestBody, String iamRequestHeaders, Integer requestTimeout, boolean followRedirects) throws VaultAccessException, DefaultMuleException {
        super();
        this.client = httpClient;
        this.authMount = authMount;
        this.role = role;
        this.iamRequestUrl = iamRequestUrl;
        this.iamRequestBody = iamRequestBody;
        this.iamRequestHeaders = iamRequestHeaders;
        this.vaultUrl = vaultUrl;
        this.engineVersion = engineVersion;
        this.requestTimeout = requestTimeout;
        this.followRedirects = followRedirects;

        this.token = authenticate();
        this.vConfig = new com.avioconsulting.mule.vault.api.client.VaultConfig(this.client, this.vaultUrl, requestTimeout, this.token, this.engineVersion.getEngineVersionNumber(), followRedirects);
    }

    public String authenticate() throws VaultAccessException, DefaultMuleException {
        String token = null;
        String mount = "aws";

        if (this.authMount != null && !this.authMount.isEmpty()) {
            mount = this.authMount;
        }

        HttpRequestBuilder builder = HttpRequest.builder().
                uri(this.vaultUrl + "/v1/auth/" + mount + "/login").
                method(HttpConstants.Method.POST);

        JsonObject payload = new JsonObject();
        if (this.role != null) {
            payload.addProperty("role", this.role);
        }

        payload.addProperty("iam_http_request_method", "POST");
        payload.addProperty("iam_request_url", this.iamRequestUrl);
        payload.addProperty("iam_request_headers", this.iamRequestHeaders);
        payload.addProperty("iam_request_body", this.iamRequestBody);
        builder.entity(new ByteArrayHttpEntity(payload.toString().getBytes()));

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
                JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
                throw new VaultAccessException(new Exception("Access Error received from Vault: " + response.getStatusCode() + ", Detail: " + elem.toString()));
            } else {
                JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
                throw new DefaultMuleException(new Exception("Unknown error received from Vault: " + response.getStatusCode()) + ", Detail: " + elem.toString());
            }

        } catch (InterruptedException | ExecutionException e ) {
            logger.error("Exception encountered while authenticating", e);
            throw new DefaultMuleException(e);
        }

        return token;
    }

    @Override
    public boolean isValid() {
        if (this.token == null || this.token.isEmpty()) {
            try {
                this.token = authenticate();
            } catch (VaultAccessException | DefaultMuleException e) {
                logger.error("Error Authenticating", e);
            }
        }
        return this.token != null && !this.token.isEmpty();
    }
}
