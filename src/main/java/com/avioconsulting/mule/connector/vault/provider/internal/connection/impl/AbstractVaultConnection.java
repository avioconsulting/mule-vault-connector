package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.VaultResponseAttributes;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.UnknownVaultException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.internal.configuration.ConfigurationOverrides;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.google.gson.*;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class implementing common methods on a VaultConnection
 *
 * @author Adam Mead
 */
public abstract class AbstractVaultConnection implements VaultConnection {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVaultConnection.class);

    String id;
    boolean valid = false;
    Vault vault;
    VaultConfig vaultConfig;

    // using local config
    com.avioconsulting.mule.vault.api.client.VaultConfig vConfig;
    private static final String VAULT_TOKEN_HEADER = "X-Vault-Token";

    boolean renewable;
    Instant expirationTime;

    protected HttpClient client;
    protected String token;
    protected String vaultUrl;
    protected Integer responseTimeout;
    protected TimeUnit responseTimeoutUnit;
    protected Boolean followRedirects;

    public AbstractVaultConnection() {
        id = null;
        vault = null;
        vaultConfig = new VaultConfig();
        renewable = false;
        expirationTime = Clock.systemDefaultZone().instant();

        vConfig = new com.avioconsulting.mule.vault.api.client.VaultConfig();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Vault getVault() {
        return vault;
    }

    @Override
    public void invalidate() {
        this.valid = false;
        this.vault = null;
    }

    @Override
    public boolean isValid() {
        if (expirationTime != null) {
            if (expirationTime.isBefore(Clock.systemDefaultZone().instant())) {
                renewLease();
            } else {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Renew the Vault Token to keep it valid
     */
    @Override
    public void renewLease() {
        if (renewable && expirationTime != null && expirationTime.isBefore(Clock.systemDefaultZone().instant())) {
            try {
                AuthResponse response = vault.auth().renewSelf();
                this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
                this.vault = new Vault(this.vaultConfig.build());
                this.renewable = response.getRenewable();
                this.expirationTime = Clock.systemDefaultZone().instant().plusSeconds(response.getAuthLeaseDuration());
            } catch (VaultException ve) {
                logger.error("Error renewing Vault token",ve);
            }
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> getSecret(String path, ConfigurationOverrides overrides) throws VaultAccessException, SecretNotFoundException, UnknownVaultException {
        try {
            HttpResponse response = read(path, overrides);
            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> builder = Result.builder();
            return builder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.toString().getBytes())).
                    length(responseData.toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new UnknownVaultException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new UnknownVaultException(e);
        } catch (com.avioconsulting.mule.vault.api.client.exception.VaultException e) {
            e.printStackTrace();
            if (e.getStatusCode() == 404) {
                logger.error("Secret not found in Vault", e);
                throw new SecretNotFoundException(e);
            } else if (e.getStatusCode() == 403) {
                logger.error("Access denied in Vault", e);
                throw new VaultAccessException(e);
            } else {
                logger.error("Unknown Vault Exception", e);
                throw new UnknownVaultException(e);
            }
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> writeSecret(String path, String secret, ConfigurationOverrides overrides) throws VaultAccessException, UnknownVaultException {
        try {
            logger.info("writeSecret() Is writing: " + secret);
            HttpResponse response = write(path, secret, overrides);
            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> builder = Result.builder();
            return builder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.toString().getBytes())).
                    length(responseData.toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();

        } catch (com.avioconsulting.mule.vault.api.client.exception.VaultException e) {
            e.printStackTrace();
            if (e.getStatusCode() == 403) {
                logger.error("Access denied in Vault", e);
                throw new VaultAccessException(e);
            } else {
                logger.error("Unknown Vault Exception", e);
                throw new UnknownVaultException(e);
            }
        } catch (Exception e) {
            throw new UnknownVaultException(e);
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> encryptData(String transitMountpoint, String keyName, String plaintext, ConfigurationOverrides overrides) throws VaultAccessException, UnknownVaultException {
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("plaintext", plaintext);
            logger.info("encrypt() Sending: " + jo.toString());

            HttpResponse response = write(transitMountpoint + "/encrypt/" + keyName, jo.toString(), overrides);
            JsonObject responseData = handleResponse(response);
            Result.Builder<InputStream, VaultResponseAttributes> builder = Result.builder();
            return builder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(responseData.get("ciphertext").toString().getBytes())).
                    length(responseData.get("ciphertext").toString().length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (com.avioconsulting.mule.vault.api.client.exception.VaultException ve) {
            if (ve.getStatusCode() == 403) {
                logger.error("Access denied in Vault", ve);
                throw new VaultAccessException(ve);
            } else {
                logger.error("Unknown Vault Exception", ve);
                throw new UnknownVaultException(ve);
            }
        } catch (Exception e) {
            logger.error("Unknown Vault Exception", e);
            throw new UnknownVaultException(e);
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> decryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws VaultAccessException, UnknownVaultException {
        try {

            JsonObject jo = new JsonObject();
            jo.addProperty("ciphertext", ciphertext);
            logger.info("decrypt() Sending: " + jo.toString());

            HttpResponse response = write(transitMountpoint + "/decrypt/" + keyName, jo.toString(), overrides);
            JsonObject responseObject = handleResponse(response);

            logger.info("decrypt() returned: " + response.toString());
            String encodedText = responseObject.get("plaintext").toString();
            logger.info("decrypt() plaintext: " + encodedText);

            Result.Builder<InputStream, VaultResponseAttributes> builder = Result.builder();
            return builder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(encodedText.getBytes())).
                    length(encodedText.length()).
                    mediaType(MediaType.APPLICATION_JSON).build();

        } catch (com.avioconsulting.mule.vault.api.client.exception.VaultException ve) {
            ve.printStackTrace();
            if (ve.getStatusCode() == 403) {
                logger.error("Access denied in Vault", ve);
                throw new VaultAccessException(ve);
            } else {
                logger.error("Unknown Vault Exception", ve);
                throw new UnknownVaultException(ve);
            }
        } catch (Exception e) {
            logger.error("Unknown Vault Exception", e);
            throw new UnknownVaultException(e);
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> reencryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws VaultAccessException, UnknownVaultException {
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("ciphertext", ciphertext);
            logger.info("re-encrypt() Sending: " + jo.toString());

            HttpResponse response = write(transitMountpoint + "/rewrap/" + keyName, jo.toString(), overrides);
            JsonObject responseData = handleResponse(response);
            String reencryptedText = responseData.get("ciphertext").toString();
            Result.Builder<InputStream, VaultResponseAttributes> builder = Result.builder();
            return builder.attributes(new VaultResponseAttributes(response)).
                    output(new ByteArrayInputStream(reencryptedText.getBytes())).
                    length(reencryptedText.length()).
                    mediaType(MediaType.APPLICATION_JSON).build();
        } catch (com.avioconsulting.mule.vault.api.client.exception.VaultException ve) {
            ve.printStackTrace();
            if (ve.getStatusCode() == 403) {
                logger.error("Access denied in Vault", ve);
                throw new VaultAccessException(ve);
            } else {
                logger.error("Unknown Vault Exception", ve);
                throw new UnknownVaultException(ve);
            }
        } catch (Exception e) {
            logger.error("Unknown Vault Exception", e);
            throw new UnknownVaultException(e);
        }
    }


    // implement http methods for reading a secret.
    private HttpResponse read(String path, ConfigurationOverrides overrides) throws com.avioconsulting.mule.vault.api.client.exception.VaultException, InterruptedException, ExecutionException {
        JsonElement secretData = new JsonObject();
        HttpRequestBuilder builder = HttpRequest.builder().
                uri(vConfig.getApiBaseUrl() + massagePath(path, overrides.getEngineVersion().getEngineVersionNumber())).
                addHeader(VAULT_TOKEN_HEADER, vConfig.getToken()).
                method((HttpConstants.Method.GET));
        logger.info("read() Uri: " + builder.getUri() + " and header: " + vConfig.getToken());
        CompletableFuture<HttpResponse> completable = vConfig.getHttpClient().sendAsync(builder.build(), (int) overrides.getResponseTimeoutUnit().toMillis(overrides.getResponseTimeout()), overrides.isFollowRedirects(), null);

        HttpResponse response = completable.get();
        return response;
    }

    // implement http methods for writing a secret.
    private HttpResponse write(String path, String secretData, ConfigurationOverrides overrides) throws com.avioconsulting.mule.vault.api.client.exception.VaultException, InterruptedException, ExecutionException {
        HttpRequestBuilder builder = HttpRequest.builder().
                uri(vConfig.getApiBaseUrl() + massagePath(path, overrides.getEngineVersion().getEngineVersionNumber())).
                addHeader(VAULT_TOKEN_HEADER, vConfig.getToken()).
                method(HttpConstants.Method.POST).
//                switched from input stream entity, to byte array
        entity(new ByteArrayHttpEntity(secretData.getBytes()));

        CompletableFuture<HttpResponse> completable = vConfig.getHttpClient().sendAsync(builder.build(), (int) overrides.getResponseTimeoutUnit().toMillis(overrides.getResponseTimeout()), overrides.isFollowRedirects(), null);
        HttpResponse response = completable.get();
        return response;
    }
    // helper method to update path for v1 vs v2
    private String massagePath(final String path, final int kvVersion) {
        String massagedPath = path;
        if (path.startsWith("/")) {
            massagedPath = path.substring(1);
        }

        if (kvVersion == 2) {
            String[] splitPath = massagedPath.split("/");
            StringBuilder sb = new StringBuilder();
            sb.append(splitPath[0]);
            sb.append("/data");
            for (int i = 1; i < splitPath.length; i++) {
                sb.append("/");
                sb.append(splitPath[i]);
            }
            massagedPath = sb.toString();
        }
        logger.info("messagePath() Message Path: " + massagedPath);
        return massagedPath;
    }

    private JsonObject handleResponse(HttpResponse response) throws com.avioconsulting.mule.vault.api.client.exception.VaultException {
        if (response.getStatusCode() == 200 && response.getEntity() != null) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonObject = elem.getAsJsonObject();
            return jsonObject.getAsJsonObject("data");
        } else if (response.getStatusCode() >= 400) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            throw new com.avioconsulting.mule.vault.api.client.exception.VaultException(response.getStatusCode(), elem != null ? elem.toString() : "");
        }
        return new JsonObject();
    }
}
