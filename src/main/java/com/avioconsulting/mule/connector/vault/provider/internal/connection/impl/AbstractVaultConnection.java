package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.UnknownVaultException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.google.gson.*;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
    protected EngineVersion engineVersion;
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
    public String getSecret(String path) throws VaultAccessException, SecretNotFoundException, UnknownVaultException {
        try {
            Gson gson = new GsonBuilder().create();
//                return gson.toJson(getVault().logical().read(path).getData());

            return gson.toJson(read(path));
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
    public void writeSecret(String path, String secret) throws VaultAccessException, UnknownVaultException {
        try {
//            Gson gson = new Gson();
//            Type secretType = new TypeToken<Map<String, Object>>() {}.getType();
//            Map<String, Object> secretData = gson.fromJson(secret, secretType);
//            getVault().logical().write(path, secretData);

            logger.info("writeSecret() Is writing: " + secret);
            write(path, secret);
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
            e.printStackTrace();
            throw new UnknownVaultException(e);
        }
    }

    @Override
    public String encryptData(String transitMountpoint, String keyName, String plaintext) throws VaultAccessException, UnknownVaultException {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("plaintext", Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8)));
//            LogicalResponse response = getVault().logical().write(transitMountpoint + "/encrypt/" + keyName, data);
//            return response.getData().get("ciphertext");

            JsonObject jo = new JsonObject();
            String encodedText = Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8));
            jo.addProperty("plaintext", encodedText);
            logger.info("encrypt() Sending: " + jo.toString());
            JsonObject response = write(transitMountpoint + "/encrypt/" + keyName, jo.toString());
            return response.get("ciphertext").toString();
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
    public String decryptData(String transitMountpoint, String keyName, String ciphertext) throws VaultAccessException, UnknownVaultException {
        try {
//            Map<String, Object> data = new HashMap<>();
//            data.put("ciphertext", ciphertext);
//            LogicalResponse response = getVault().logical().write(transitMountpoint + "/decrypt/" + keyName, data);
//            String decrypted = new String(Base64.getDecoder().decode(response.getData().get("plaintext")), StandardCharsets.UTF_8);
//            return decrypted;

            JsonObject jo = new JsonObject();
            jo.addProperty("ciphertext", ciphertext);
            logger.info("decrypt() Sending: " + jo.toString());
            JsonObject response = write(transitMountpoint + "/decrypt/" + keyName, jo.toString());

            logger.info("decrypt() returned: " + response.toString());
            String encodedText = response.get("plaintext").getAsString();
            logger.info("decrypt() plaintext: " + encodedText);

            String decrypted = new String(Base64.getDecoder().decode(encodedText), StandardCharsets.UTF_8);
            return decrypted;

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
    public String reencryptData(String transitMountpoint, String keyName, String ciphertext) throws VaultAccessException, UnknownVaultException {
        try {
//            Map<String, Object> data = new HashMap<>();
//            data.put("ciphertext", ciphertext);
//            LogicalResponse response = getVault().logical().write(transitMountpoint + "/rewrap/" + keyName, data);
//            return response.getData().get("ciphertext");

            JsonObject jo = new JsonObject();
            jo.addProperty("plaintext", ciphertext);
            logger.info("re-encrypt() Sending: " + jo.toString());
            JsonObject response = write(transitMountpoint + "/rewrap/" + keyName, jo.toString());
            return response.get("ciphertext").toString();
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
    private JsonElement read(String path) throws com.avioconsulting.mule.vault.api.client.exception.VaultException, InterruptedException, ExecutionException {
        JsonElement secretData = new JsonObject();
        HttpRequestBuilder builder = HttpRequest.builder().
                uri(vConfig.getApiBaseUrl() + massagePath(path)).
                addHeader(VAULT_TOKEN_HEADER, vConfig.getToken()).
                method((HttpConstants.Method.GET));
        logger.info("read() Uri: " + builder.getUri() + " and header: " + vConfig.getToken());
        CompletableFuture<HttpResponse> completable = vConfig.getHttpClient().sendAsync(builder.build(), vConfig.getTimeoutInMilliseconds(), vConfig.isFollowRedirects(), null);

        HttpResponse response = completable.get();
        logger.info("read()  Response Code - " + response.getStatusCode());
        if (response.getStatusCode() == 200 && response.getEntity() != null) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonObject = elem.getAsJsonObject();
            secretData = jsonObject.get("data");
        } else if (response.getStatusCode() >= 400) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            throw new com.avioconsulting.mule.vault.api.client.exception.VaultException(response.getStatusCode(), elem.toString());
        }
        return secretData;
    }

    // implement http methods for writing a secret.
    private JsonObject write(String path, String secretData) throws com.avioconsulting.mule.vault.api.client.exception.VaultException, InterruptedException, ExecutionException {
        JsonObject secretMetadata = new JsonObject();
        HttpRequestBuilder builder = HttpRequest.builder().
                uri(vConfig.getApiBaseUrl() + massagePath(path)).
                addHeader(VAULT_TOKEN_HEADER, vConfig.getToken()).
                method(HttpConstants.Method.POST).
//                switched from input stream entity, to byte array
        entity(new ByteArrayHttpEntity(secretData.getBytes()));

        CompletableFuture<HttpResponse> completable = vConfig.getHttpClient().sendAsync(builder.build(), vConfig.getTimeoutInMilliseconds(), vConfig.isFollowRedirects(), null);
        HttpResponse response = completable.get();

        if (response.getStatusCode() == 200 && response.getEntity() != null) {
            // This is KV-V2
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonObject = elem.getAsJsonObject();
            return jsonObject.getAsJsonObject("data");
        } else if (response.getStatusCode() >= 400) {
            JsonElement elem = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
            throw new com.avioconsulting.mule.vault.api.client.exception.VaultException(response.getStatusCode(), elem.toString());
        }
        return secretMetadata;
    }
    // helper method to update path for v1 vs v2
    private String massagePath(final String path) {
        String massagedPath = path;
        if (path.startsWith("/")) {
            massagedPath = path.substring(1);
        }

        String[] splitPath = massagedPath.split("/");
        StringBuilder sb = new StringBuilder();
        sb.append(splitPath[0]);
        logger.info("messagePath()) Found kvVersion: " + vConfig.getKvVersion());
        if (vConfig.getKvVersion() == 2) {
            sb.append("/data");
        }
        for (int i = 1; i < splitPath.length; i++) {
            sb.append("/");
            sb.append(splitPath[i]);
        }
        logger.info("messagePath() Message Path: " + sb.toString());
        return sb.toString();
    }

}
