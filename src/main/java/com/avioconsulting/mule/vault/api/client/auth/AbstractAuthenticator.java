package com.avioconsulting.mule.vault.api.client.auth;

import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.VaultRequestBuilder;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AbstractAuthenticator implements VaultAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthenticator.class);

    @Override
    public String authenticate(VaultConfig config) throws AccessException, VaultException, InterruptedException {
        String token = null;

        try {
            HttpRequestBuilder builder = new VaultRequestBuilder().
                    config(config).
                    kvVersion(1).
                    secretPath(getAuthPath()).
                    payload(getAuthPayload(config)).
                    build().
                    getHttpRequestBuilder().
                    method(HttpConstants.Method.POST);

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), config.getTimeoutInMilliseconds(), config.isFollowRedirects(), null);

            HttpResponse response = completable.get();

            if (response.getStatusCode() == 200 && response.getEntity() != null) {
                token = extractToken(response.getEntity());
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
        } catch (ExecutionException e ) {
            throw new VaultException(e);
        }

        return token;
    }

    @Override
    public String getAuthPath() {
        return "auth/token/login";
    }

    @Override
    public String getAuthPayload(VaultConfig config) throws VaultException {
        return null;
    }

    private String extractToken(HttpEntity entity) {
        String token = null;
        JsonElement elem = JsonParser.parseReader(new InputStreamReader(entity.getContent()));
        JsonElement authData = elem.getAsJsonObject().get("auth");
        if (authData != null) {
            JsonElement clientToken = authData.getAsJsonObject().get("client_token");
            token = clientToken.getAsString();
            logger.info("Retrieved client token");
        }
        return token;
    }

}
