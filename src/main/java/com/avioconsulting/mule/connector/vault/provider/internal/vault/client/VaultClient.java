package com.avioconsulting.mule.connector.vault.provider.internal.vault.client;

import com.avioconsulting.mule.connector.vault.provider.api.VaultResponseAttributes;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.AccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.VaultException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VaultClient {

    private static final Logger logger = LoggerFactory.getLogger(VaultClient.class);

    private String token;

    private VaultConfig config;

    public VaultClient(VaultConfig config) {
        super();
        this.config = config;
    }

    public void invalidate() {
        this.token = null;
    }

    public String getToken() {
        return token;
    }

    public void authenticate() throws AccessException, VaultException, InterruptedException {
        this.token = config.getAuthenticator().authenticate(config);
    }

    public Result<InputStream, VaultResponseAttributes> getSecret(final VaultRequest request) throws AccessException, SecretNotFoundException, VaultException, InterruptedException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.GET);

            logger.info("Getting secret from {}", builder.getUri());

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);

            HttpResponse response = completable.get();
            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> resultBuilder = Result.builder();
            return resultBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.toString().getBytes())).
                    length(responseData.toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (ExecutionException e) {
            throw new VaultException(e);
        }
    }

    public Result<InputStream, VaultResponseAttributes> writeSecret(final VaultRequest request) throws AccessException, SecretNotFoundException, VaultException, InterruptedException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.POST).
                    entity(new ByteArrayHttpEntity(request.getPayload().getBytes()));

            logger.info("Writing secret to {}", builder.getUri());

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);
            HttpResponse response = completable.get();

            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> responseBuilder = Result.builder();
            return responseBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.toString().getBytes())).
                    length(responseData.toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (ExecutionException e) {
            throw new VaultException(e);
        }


    }

    public Result<InputStream, VaultResponseAttributes> encryptData(final VaultRequest request) throws AccessException, SecretNotFoundException, VaultException, InterruptedException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.POST).
                    entity(new ByteArrayHttpEntity(request.getPayload().getBytes()));

            logger.info("Encrypting data via {}", builder.getUri());

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);
            HttpResponse response = completable.get();

            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> responseBuilder = Result.builder();
            return responseBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.get(VaultConstants.CIPHERTEXT_ATTRIBUTE).toString().getBytes())).
                    length(responseData.get(VaultConstants.CIPHERTEXT_ATTRIBUTE).toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (ExecutionException e) {
            throw new VaultException(e);
        }
    }

    public Result<InputStream, VaultResponseAttributes> decryptData(VaultRequest request) throws AccessException, SecretNotFoundException, VaultException, InterruptedException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.POST).
                    entity(new ByteArrayHttpEntity(request.getPayload().getBytes()));

            logger.info("Decrypting data via {}", builder.getUri());

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);
            HttpResponse response = completable.get();

            JsonObject responseObject = handleResponse(response);

            String encodedText = responseObject.get(VaultConstants.PLAINTEXT_ATTRIBUTE).toString();

            Result.Builder<InputStream, VaultResponseAttributes> responseBuilder = Result.builder();
            return responseBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(encodedText.getBytes())).
                    length(encodedText.length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (ExecutionException e) {
            throw new VaultException(e);
        }
    }

    public Result<InputStream, VaultResponseAttributes> reencryptData(VaultRequest request) throws AccessException, SecretNotFoundException, VaultException, InterruptedException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.POST).
                    entity(new ByteArrayHttpEntity(request.getPayload().getBytes()));

            logger.info("Re-encrypting data via {}", builder.getUri());

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);
            HttpResponse response = completable.get();

            JsonObject responseData = handleResponse(response);
            String reencryptedText = responseData.get(VaultConstants.CIPHERTEXT_ATTRIBUTE).toString();
            Result.Builder<InputStream, VaultResponseAttributes> responseBuilder = Result.builder();
            return responseBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(reencryptedText.getBytes())).
                    length(reencryptedText.length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (ExecutionException e) {
            throw new VaultException(e);
        }
    }

    private JsonObject handleResponse(HttpResponse response) throws SecretNotFoundException, AccessException, VaultException {
        if (response.getStatusCode() == 200 && response.getEntity() != null) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonObject = elem.getAsJsonObject();
            logger.info("Received successful response (data omitted for security)");
            return jsonObject.getAsJsonObject(VaultConstants.DATA_ATTRIBUTE);
        } else if (response.getStatusCode() == 201) {
            logger.info("Received successful response. No data included.");
            return new JsonObject();
        } else if (response.getStatusCode() >= 400) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            String message = elem != null ? elem.toString() : "";
            logger.error("Received error response. Status code ({}). Message: {}", response.getStatusCode(), message);
            if (response.getStatusCode() == 403) {
                throw new AccessException(message);
            } else if (response.getStatusCode() == 404) {
                throw new SecretNotFoundException(message);
            } else {
                throw new VaultException(response.getStatusCode(), message);
            }
        }
        return new JsonObject();
    }

}
