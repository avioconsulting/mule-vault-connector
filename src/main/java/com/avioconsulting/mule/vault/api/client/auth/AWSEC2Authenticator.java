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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AWSEC2Authenticator implements VaultAuthenticator {

    // This is the URI to use to retrieve the PKCS7 Signature
    // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
    private static final String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";
    private static final String CUSTOM_IMDS_URI_PROPERTY = "CUSTOM_IMDS";
    private static final Logger logger = LoggerFactory.getLogger(AWSEC2Authenticator.class);

    private String authMount;
    private String identity;
    private String nonce;
    private String pkcs7;
    private String role;
    private String signature;
    private boolean useInstanceMetadata;

    public AWSEC2Authenticator(String authMount, String awsRole, String pkcs7, String nonce, String identity, String signature, boolean useInstanceMetadata) {
        this.authMount = authMount;
        this.role = awsRole;
        this.pkcs7 = pkcs7;
        this.nonce = nonce;
        this.identity = identity;
        this.signature = signature;
        this.useInstanceMetadata = useInstanceMetadata;
    }

    @Override
    public String authenticate(VaultConfig config) throws AccessException, VaultException, InterruptedException {
        String token = null;
        String mount = "aws";

        if (useInstanceMetadata) {
            pkcs7 = lookupPKCS7(config);
        }
        boolean pkcsUnavailable = pkcs7 == null || pkcs7.isEmpty();
        boolean identityUnavailable = identity == null || identity.isEmpty() || signature == null || signature.isEmpty();
        if (pkcsUnavailable && identityUnavailable) {
            logger.error("PKCS7 Signature, Identity Document, and Identity Signature are all null or empty");
            throw new VaultException("PKCS7 Signature or the Identity Document and Signature are required for authentication");
        }

        if (authMount != null && !authMount.isEmpty()) {
            mount = authMount;
        }

        String authUri = String.format("%s%s/auth/%s/login", config.getBaseUrl(), VaultConstants.VAULT_API_PATH, mount);
        logger.info(String.format("Authenticating at %s", authUri));

        HttpRequestBuilder builder = HttpRequest.builder().
                uri(authUri).
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
        if (this.nonce != null) {
            payload.addProperty("nonce", this.nonce);
        } else {
            logger.warn("No nonce provided. Re-authentication may not be possible.");
        }
        if (!pkcsUnavailable) {
            payload.addProperty("pkcs7", pkcs7);
        } else {
            payload.addProperty("identity", this.identity);
            payload.addProperty("signature", this.signature);
        }
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
                    logger.info("Retrieved client token");
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

    /**
     * EC2 Provides a service to retrieve the instance identity. This method uses that service to look up the PKCS7.
     *
     * @return the PKCS7 value with the '\n' characters removed
     */
    private String lookupPKCS7(VaultConfig config) throws VaultException {
        String pkcs7;
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
        CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), config.getTimeoutInMilliseconds(), config.isFollowRedirects(), null);

        try {
            HttpResponse response = completable.get();

            if (response.getStatusCode() == 200) {
                pkcs7 = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining());
            } else {
                throw new VaultException(response.getStatusCode(), "Error received from Metadata Service");
            }

        } catch (InterruptedException | ExecutionException e ) {
            logger.error("Exception encountered while retrieving PKCS7 from IMDS", e);
            throw new VaultException(e);
        }

        return pkcs7;
    }
}
