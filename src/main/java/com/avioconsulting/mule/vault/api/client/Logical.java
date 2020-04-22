package com.avioconsulting.mule.vault.api.client;

import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Logical {

    private static final String VAULT_TOKEN_HEADER = "X-Vault-Token";

    private VaultConfig config;

    public Logical(VaultConfig config) {
        this.config = config;
    }

    private String massagePath(final String path) {
        String massagedPath = path;
        if (path.startsWith("/")) {
            massagedPath = path.substring(1);
        }

        String[] splitPath = massagedPath.split("/");
        StringBuilder sb = new StringBuilder();
        sb.append(splitPath[0]);
        if (config.getKvVersion() == 2) {
            sb.append("/data");
        }
        for (int i = 1; i < splitPath.length; i++) {
            sb.append("/");
            sb.append(splitPath[i]);
        }
        return sb.toString();
    }

    public JsonObject read(String path) throws VaultException, InterruptedException, ExecutionException {
        JsonObject secretData = new JsonObject();
        HttpRequestBuilder builder = HttpRequest.builder().
                uri(config.getApiBaseUrl() + massagePath(path)).
                addHeader(VAULT_TOKEN_HEADER, config.getToken()).
                method((HttpConstants.Method.GET));

        CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), config.getTimeout(), true, null);

        HttpResponse response = completable.get();

        if (response.getStatusCode() == 200 && response.getEntity() != null) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonObject = elem.getAsJsonObject();
            secretData = jsonObject.getAsJsonObject("data");
        } else if (response.getStatusCode() >= 400) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            throw new VaultException(response.getStatusCode(), elem.toString());
        }
        return secretData;
    }

    public JsonObject write(String path, InputStream secretData) throws VaultException, InterruptedException, ExecutionException {
        JsonObject secretMetadata = new JsonObject();
        HttpRequestBuilder builder = HttpRequest.builder().
                uri(config.getApiBaseUrl() + massagePath(path)).
                addHeader(VAULT_TOKEN_HEADER, config.getToken()).
                method(HttpConstants.Method.POST).
                entity(new InputStreamHttpEntity(secretData));

        CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), config.getTimeout(), true, null);

        HttpResponse response = completable.get();

        if (response.getStatusCode() == 200 && response.getEntity() != null) {
            // This is KV-V2
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonObject = elem.getAsJsonObject();
            return jsonObject.getAsJsonObject("data");
        } else if (response.getStatusCode() >= 400) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            throw new VaultException(response.getStatusCode(), elem.getAsString());
        }
        return secretMetadata;
    }

}
