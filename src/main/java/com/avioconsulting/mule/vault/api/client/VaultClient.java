package com.avioconsulting.mule.vault.api.client;

import com.avioconsulting.mule.connector.vault.provider.api.VaultResponseAttributes;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.SecretNotFoundException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
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

    public void authenticate() throws AccessException, VaultException {
        this.token = config.getAuthenticator().authenticate(config);
    }

    public boolean validateToken() throws VaultException {
        boolean valid = false;
        HttpRequestBuilder builder = HttpRequest.builder();
        builder.uri(config.getBaseUrl() + VaultConstants.VAULT_API_PATH + "/auth/token/lookup" );
        builder.addHeader("X-Vault-Token", token);
        builder.method(HttpConstants.Method.GET);
        logger.info("isValid() " + builder.build().toString());
        CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), config.getTimeoutInMilliseconds(), config.isFollowRedirects(), null);

        try {
            HttpResponse response = completable.get();

            logger.info("isValid() Response: " + response.getStatusCode() + " " + response.toString());
            if (response.getStatusCode() == 404) {
                logger.error("Secret not found in Vault");
            } else if (response.getStatusCode() == 403) {
                logger.error("Access denied in Vault");
            } else if (response.getStatusCode() > 299){
                logger.error("Unknown Vault Exception");
            } else {
                valid = true;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new VaultException(e);
        }

        return valid;
    }

    public Result<InputStream, VaultResponseAttributes> getSecret(final VaultRequest request) throws AccessException, SecretNotFoundException, VaultException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.GET);

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);

            HttpResponse response = completable.get();
            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> resultBuilder = Result.builder();
            return resultBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.toString().getBytes())).
                    length(responseData.toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (ExecutionException | InterruptedException e) {
            throw new VaultException(e);
        }
    }

    public Result<InputStream, VaultResponseAttributes> writeSecret(final VaultRequest request) throws AccessException, SecretNotFoundException, VaultException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.POST).
                    entity(new ByteArrayHttpEntity(request.getPayload().getBytes()));

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);
            HttpResponse response = completable.get();

            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> responseBuilder = Result.builder();
            return responseBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.toString().getBytes())).
                    length(responseData.toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();

        } catch (InterruptedException | ExecutionException e) {
            throw new VaultException(e);
        }
    }

    public Result<InputStream, VaultResponseAttributes> encryptData(final VaultRequest request) throws AccessException, SecretNotFoundException, VaultException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.POST).
                    entity(new ByteArrayHttpEntity(request.getPayload().getBytes()));

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);
            HttpResponse response = completable.get();

            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> responseBuilder = Result.builder();
            return responseBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.get("ciphertext").toString().getBytes())).
                    length(responseData.get("ciphertext").toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (ExecutionException | InterruptedException e) {
            throw new VaultException(e);
        }
    }

    public Result<InputStream, VaultResponseAttributes> decryptData(VaultRequest request) throws AccessException, SecretNotFoundException, VaultException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.POST).
                    entity(new ByteArrayHttpEntity(request.getPayload().getBytes()));

            logger.info("URI: " + builder.getUri());

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);
            HttpResponse response = completable.get();

            JsonObject responseObject = handleResponse(response);

            logger.info("decrypt() returned: " + response.toString());
            String encodedText = responseObject.get("plaintext").toString();
            logger.info("decrypt() plaintext: " + encodedText);

            Result.Builder<InputStream, VaultResponseAttributes> responseBuilder = Result.builder();
            return responseBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(encodedText.getBytes())).
                    length(encodedText.length()).
                    mediaType(MediaType.APPLICATION_JSON).build();

        } catch (ExecutionException | InterruptedException e) {
            throw new VaultException(e);
        }
    }

    public Result<InputStream, VaultResponseAttributes> reencryptData(VaultRequest request) throws AccessException, SecretNotFoundException, VaultException {
        try {
            HttpRequestBuilder builder = request.getHttpRequestBuilder().
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token).
                    method(HttpConstants.Method.POST).
                    entity(new ByteArrayHttpEntity(request.getPayload().getBytes()));

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), request.getResponseTimeout(), request.isFollowRedirects(), null);
            HttpResponse response = completable.get();

            JsonObject responseData = handleResponse(response);
            String reencryptedText = responseData.get("ciphertext").toString();
            Result.Builder<InputStream, VaultResponseAttributes> responseBuilder = Result.builder();
            return responseBuilder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(reencryptedText.getBytes())).
                    length(reencryptedText.length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (ExecutionException | InterruptedException e) {
            throw new VaultException(e);
        }
    }

    private JsonObject handleResponse(HttpResponse response) throws SecretNotFoundException, AccessException, VaultException {
        if (response.getStatusCode() == 200 && response.getEntity() != null) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonObject = elem.getAsJsonObject();
            return jsonObject.getAsJsonObject("data");
        } else if (response.getStatusCode() == 201) {
            return new JsonObject();
        } else if (response.getStatusCode() >= 400) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            String message = elem != null ? elem.toString() : "";
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
