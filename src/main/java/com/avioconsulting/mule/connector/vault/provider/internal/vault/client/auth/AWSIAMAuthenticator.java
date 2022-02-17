package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.algorithm.AWSV4Auth;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.algorithm.AWSV4SignProperties;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.VaultException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class AWSIAMAuthenticator extends AbstractAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(AWSIAMAuthenticator.class);

    private static final String DEFAULT_AUTH_MOUNT = "aws";

    private String authMount;
    private String role;
    private String iamRequestUrl;
    private String iamRequestBody;
    private String iamServerId;
    private String awsAccessKey;
    private String awsSecretKey;

    public AWSIAMAuthenticator(String authMount, String role, String iamRequestUrl, String iamRequestBody,
                               String iamServerId, String awsAccessKey, String awsSecretKey) {
        super();
        this.authMount = authMount;
        this.role = role;
        this.iamRequestUrl = iamRequestUrl;
        this.iamRequestBody = iamRequestBody;
        this.iamServerId = iamServerId;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
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
    public String getAuthPayload(VaultConfig config) throws VaultException {
        JsonObject payload = new JsonObject();
        if (this.role != null) {
            payload.addProperty("role", this.role);
        }

        payload.addProperty("iam_http_request_method", "POST");
        payload.addProperty("iam_request_url", encodeBase64(iamRequestUrl));
        payload.addProperty("iam_request_headers", encodeBase64(generateHeaders()));
        payload.addProperty("iam_request_body", encodeBase64(iamRequestBody));
        return payload.toString();
    }

    private String generateHeaders() {

        AWSV4SignProperties awsV4SignProperties = new AWSV4SignProperties(iamRequestUrl);

        TreeMap<String, String> awsHeaders = new TreeMap();
        awsHeaders.put("host", awsV4SignProperties.getHost());
        getIamServerId().ifPresent(serverId -> awsHeaders.put("x-vault-aws-iam-server-id", serverId));

        AWSV4Auth awsV4Auth = new AWSV4Auth.Builder(awsAccessKey, awsSecretKey)
                .regionName(awsV4SignProperties.getRegion())
                .serviceName(awsV4SignProperties.getServiceName())
                .httpMethodName(awsV4SignProperties.getMethod())
                .canonicalURI(awsV4SignProperties.getCanonicalUri())
                .queryParametes(awsV4SignProperties.getQueryParameters())
                .awsHeaders(awsHeaders)
                .payload(iamRequestBody)
                .build();

        String authorization = awsV4Auth.getHeaders().get("Authorization");

        Map<String, String> headers = new HashMap();
        headers.put("Authorization", authorization);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("X-Amz-Date", awsV4Auth.getxAmzDate());
        getIamServerId().ifPresent(serverId -> headers.put("x-vault-aws-iam-server-id", serverId));
        Gson gson = new Gson();
        String textOfHeaders = gson.toJson(headers);

        return textOfHeaders;
    }

    private String encodeBase64(String value) {
        byte[] encoded = Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8));

        return new String(encoded);
    }

    private Optional<String> getIamServerId() {
        return Optional.ofNullable(iamServerId).filter(s -> !s.trim().isEmpty());
    }

}
