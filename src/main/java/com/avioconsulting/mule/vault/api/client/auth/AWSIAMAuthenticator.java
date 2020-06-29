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

public class AWSIAMAuthenticator implements VaultAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(AWSIAMAuthenticator.class);

    private String authMount;
    private String role;
    private String iamRequestUrl;
    private String iamRequestBody;
    private String iamRequestHeaders;

    public AWSIAMAuthenticator(String authMount, String role, String iamRequestUrl, String iamRequestBody, String iamRequestHeaders) {
        super();
        this.authMount = authMount;
        this.role = role;
        this.iamRequestUrl = iamRequestUrl;
        this.iamRequestBody = iamRequestBody;
        this.iamRequestHeaders = iamRequestHeaders;
    }

    @Override
    public String authenticate(VaultConfig config) throws AccessException, VaultException, InterruptedException {
        String token = null;
        String mount = "aws";

        if (this.authMount != null && !this.authMount.isEmpty()) {
            mount = this.authMount;
        }

        HttpRequestBuilder builder = HttpRequest.builder().
                uri(config.getBaseUrl() + VaultConstants.VAULT_API_PATH + "/auth/" + mount + "/login").
                method(HttpConstants.Method.POST);

        if (config.isIncludeVaultRequestHeader()) {
            builder.addHeader(VaultConstants.VAULT_REQUEST_HEADER, "true");
        }

        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            builder.addHeader(VaultConstants.VAULT_NAMESPACE_HEADER, config.getNamespace());
        }

        JsonObject payload = new JsonObject();
        if (this.role != null) {
            payload.addProperty("role", this.role);
        }

        payload.addProperty("iam_http_request_method", "POST");
        payload.addProperty("iam_request_url", this.iamRequestUrl);
        payload.addProperty("iam_request_headers", this.iamRequestHeaders);
        payload.addProperty("iam_request_body", this.iamRequestBody);
        builder.entity(new ByteArrayHttpEntity(payload.toString().getBytes()));

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

        } catch (ExecutionException e ) {
            logger.error("Exception encountered while authenticating", e);
            throw new VaultException(e);
        }

        return token;
    }
}
