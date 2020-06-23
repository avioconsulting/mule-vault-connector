package com.avioconsulting.mule.vault.api.client.auth;

import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.VaultConstants;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TLSAuthenticator implements VaultAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(TLSAuthenticator.class);

    private String authMount;
    private String certificateRole;

    public TLSAuthenticator(String authMount, String certificateRole) {
        this.authMount = authMount;
        this.certificateRole = certificateRole;
    }

    public String authenticate(VaultConfig config) throws AccessException, VaultException {
        String token = null;
        String mount = "cert";

        if (authMount != null && !authMount.isEmpty()) {
            mount = authMount;
        }

        HttpRequestBuilder builder = HttpRequest.builder().
                uri(config.getBaseUrl() + VaultConstants.VAULT_API_PATH + "/auth/" + mount + "/login").
                method(HttpConstants.Method.POST);

        if (config.isIncludeVaultRequestHeader()) {
            builder = builder.addHeader(VaultConstants.VAULT_REQUEST_HEADER, "true");
        }

        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            builder = builder.addHeader(VaultConstants.VAULT_NAMESPACE_HEADER, config.getNamespace());
        }

        if (this.certificateRole != null && !this.certificateRole.isEmpty()) {
            JsonObject json = new JsonObject();
            json.addProperty("name", this.certificateRole);
            builder.entity(new ByteArrayHttpEntity(json.toString().getBytes()));
        }

        CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), config.getTimeoutInMilliseconds(), config.isFollowRedirects(), null);

        try {
            HttpResponse response = completable.get();

            if (response.getStatusCode() == 200 && response.getEntity() != null) {
                JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
                JsonElement authData = elem.getAsJsonObject().get("auth");
                if (authData != null) {
                    JsonElement clientToken = authData.getAsJsonObject().get("client_token");
                    token = clientToken.getAsString();
                }
            } else if (response.getStatusCode() == 201) {
                token = "";
            } else if (response.getStatusCode() >= 400) {
                JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
                String message = elem != null ? elem.toString() : "";
                if (response.getStatusCode() == 403) {
                    throw new AccessException(message);
                } else {
                    throw new VaultException(response.getStatusCode(), message);
                }
            }

        } catch (InterruptedException | ExecutionException e ) {
            logger.error("Exception encountered while authenticating", e);
            throw new VaultException(e);
        }

        return token;
    }
}
