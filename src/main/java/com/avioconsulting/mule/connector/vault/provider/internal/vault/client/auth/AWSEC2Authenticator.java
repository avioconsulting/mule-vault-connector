package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.VaultException;
import com.google.gson.JsonObject;
import org.mule.runtime.http.api.HttpConstants;
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

public class AWSEC2Authenticator extends AbstractAuthenticator {

    // This is the URI to use to retrieve the PKCS7 Signature
    // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
    private static final String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";
    private static final String CUSTOM_IMDS_URI_PROPERTY = "CUSTOM_IMDS";
    private static final String DEFAULT_AUTH_MOUNT = "aws";
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
    public String getAuthPath() {
        String mount = DEFAULT_AUTH_MOUNT;

        if (this.authMount != null && !this.authMount.isEmpty()) {
            mount = this.authMount;
        }
        logger.debug("Authentication mount: {}", mount);
        return String.format("auth/%s/login", mount);
    }

    @Override
    public String getAuthPayload(VaultConfig config) throws VaultException{
        String payload;
        if (useInstanceMetadata) {
            payload = payloadWithMetadata(config);
        } else if (isPKCS7Auth()) {
            payload = payloadWithPKCS7();
        } else if (isIdentityDocAuth()) {
            payload = payloadWithIdentityDoc();
        } else {
            throw new VaultException("PKCS7 Signature or the Identity Document and Signature are required for authentication");
        }
        return payload;
    }

    private boolean isPKCS7Auth() {
        return pkcs7 != null && !pkcs7.isEmpty();
    }

    private boolean isIdentityDocAuth() {
        return identity != null && !identity.isEmpty() && signature != null && !signature.isEmpty();
    }

    private String payloadWithMetadata(VaultConfig config) throws VaultException {
        lookupPKCS7(config);
        return payloadWithPKCS7();
    }

    private String payloadWithPKCS7() {
        JsonObject payload = addOptionalProperties(new JsonObject());
        payload.addProperty("pkcs7", pkcs7);
        return payload.toString();
    }

    private String payloadWithIdentityDoc() {
        JsonObject payload = addOptionalProperties(new JsonObject());
        payload.addProperty("identity", this.identity);
        payload.addProperty("signature", this.signature);
        return payload.toString();
    }

    private JsonObject addOptionalProperties(JsonObject json) {

        if (this.role != null) {
            json.addProperty("role", this.role);
        }
        if (this.nonce != null) {
            json.addProperty("nonce", this.nonce);
        } else {
            logger.warn("No nonce provided. Re-authentication may not be possible.");
        }

        return json;
    }

    /**
     * EC2 Provides a service to retrieve the instance identity. This method uses that service to look up the PKCS7.
     *
     * @return the PKCS7 value with the '\n' characters removed
     */
    private void lookupPKCS7(VaultConfig config) throws VaultException {
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
            throw new VaultException(e);
        }
    }
}
