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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * A connection to Vault using EC2 properties for authentication
 *
 * @author Adam Mead
 */
public class Ec2VaultConnection extends AbstractVaultConnection {

    // This is the URI to use to retrieve the PKCS7 Signature
    // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
    private static final String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";
    private static final String CUSTOM_IMDS_URI_PROPERTY = "CUSTOM_IMDS";
    private static final Logger logger = LoggerFactory.getLogger(Ec2VaultConnection.class);

    private String authMount;
    private String identity;
    private String nonce;
    private String pkcs7;
    private String role;
    private String signature;
    private boolean useInstanceMetadata;

    public Ec2VaultConnection(String vaultUrl, String authMount, String awsRole, HttpClient httpClient, EngineVersion engineVersion, String pkcs7, String nonce, String identity, String signature, boolean useInstanceMetadata, Integer requestTimeout, boolean followRedirects) throws DefaultMuleException {
        this.vaultUrl = vaultUrl;
        this.client = httpClient;
        this.engineVersion = engineVersion;
        this.authMount = authMount;
        this.role = awsRole;
        this.pkcs7 = pkcs7;
        this.nonce = nonce;
        this.identity = identity;
        this.signature = signature;
        this.useInstanceMetadata = useInstanceMetadata;
        this.requestTimeout = requestTimeout;
        this.followRedirects = followRedirects;

        this.token = authenticate();
        this.vConfig = new VaultConfig(this.client, this.vaultUrl, this.requestTimeout, this.token, this.engineVersion.getEngineVersionNumber(), this.followRedirects);
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

    public String authenticate() throws DefaultMuleException{
        String token = null;
        String mount = "aws";

        if (useInstanceMetadata) {
            pkcs7 = lookupPKCS7();
        }
        boolean pkcsUnavailable = pkcs7 == null || pkcs7.isEmpty();
        boolean identityUnavailable = identity == null || identity.isEmpty() || signature == null || signature.isEmpty();
        if (pkcsUnavailable && identityUnavailable) {
            logger.error("PKCS7 Signature, Identity Document, and Identity Signature are all null or empty");
            throw new DefaultMuleException("PKCS7 Signature or the Identity Document and Signature are required for authentication");
        }

        if (authMount != null && !authMount.isEmpty()) {
            mount = authMount;
        }

        HttpRequestBuilder builder = HttpRequest.builder().
                uri(this.vaultUrl + "/v1/auth/" + mount + "/login").
                method(HttpConstants.Method.POST);

        JsonObject payload = new JsonObject();
        if (this.role != null) {
            payload.addProperty("role", this.role);
        }
        if (this.nonce != null) {
            payload.addProperty("nonce", this.nonce);
        } else {
            logger.warn("No nonce provided. Reauthentication may not be possible.");
        }
        if (!pkcsUnavailable) {
            payload.addProperty("pkcs7", pkcs7);
        } else {
            payload.addProperty("identity", this.identity);
            payload.addProperty("signature", this.signature);
        }
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

    /**
     * EC2 Provides a service to retrieve the instance identity. This method uses that service to look up the PKCS7.
     *
     * @return the PKCS7 value with the '\n' characters removed
     */
    private String lookupPKCS7() throws DefaultMuleException {
        String pkcs7 = null;
        String imdsUri = INSTANCE_PKCS7_URI;

        // This assists in testing and will help if Amazon moves the IMDS service
        try {
            String customUri = System.getProperty(CUSTOM_IMDS_URI_PROPERTY);
            if (customUri != null && !customUri.isEmpty()) {
                imdsUri = customUri;
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException e) {
            // Typically this won't be an issue, so only logging at info level. Initial intention is for testing support.
            logger.info("Error accessing system property: " + CUSTOM_IMDS_URI_PROPERTY);
        }

        HttpRequestBuilder builder = HttpRequest.builder().
                uri(imdsUri).
                method(HttpConstants.Method.GET);
        CompletableFuture<HttpResponse> completable = client.sendAsync(builder.build(), this.requestTimeout, this.followRedirects, null);

        try {
            HttpResponse response = completable.get();

            if (response.getStatusCode() == 200) {
                pkcs7 = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining());
            } else {
                throw new DefaultMuleException(new Exception("Error received from Metadata Service: " + response.getStatusCode()));
            }

        } catch (InterruptedException | ExecutionException e ) {
            logger.error("Exception encountered while retrieving PKCS7 from IMDS", e);
            throw new DefaultMuleException(e);
        }

        return pkcs7;
    }
}
