package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.algorithm.AWSV4Auth;
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

    private static final String DEFAULT_HOST = "sts.amazonaws.com";
    private static final String DEFAULT_SERVICE_NAME = "sts";
    private static final String DEFAULT_REGION = "us-east-1";
    private static final String DEFAULT_METHOD = "POST";
    private static final String DEFAULT_PAYLOAD = "Action=GetCallerIdentity&Version=2011-06-15";
    private static final String DEFAULT_CANONICAL_URI = "/";

    private String authMount;
    private String role;
    private String iamRequestUrl;
    private String iamRequestBody;
    private String iamServerId;
    private String iamAccessKey;
    private String iamSecretKey;

    public AWSIAMAuthenticator(String authMount, String role, String iamRequestUrl, String iamRequestBody,
                               String iamServerId, String iamAccessKey, String iamSecretKey) {
        super();
        this.authMount = authMount;
        this.role = role;
        this.iamRequestUrl = iamRequestUrl;
        this.iamRequestBody = iamRequestBody;
        this.iamServerId = iamServerId;
        this.iamAccessKey = iamAccessKey;
        this.iamSecretKey = iamSecretKey;
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

        String encodedIamRequestHeaders = new String(Base64.getEncoder().encode(generateHeaders().getBytes(StandardCharsets.UTF_8)));

        payload.addProperty("iam_http_request_method", "POST");
        payload.addProperty("iam_request_url", this.iamRequestUrl);
        payload.addProperty("iam_request_headers", encodedIamRequestHeaders);
        payload.addProperty("iam_request_body", this.iamRequestBody);
        return payload.toString();
    }

    private String generateHeaders() {

        TreeMap<String, String> awsHeaders = new TreeMap();
        awsHeaders.put("host", DEFAULT_HOST);
        getIamServerId().ifPresent(serverId -> awsHeaders.put("x-vault-aws-iam-server-id", serverId));

        AWSV4Auth awsV4Auth = new AWSV4Auth.Builder(iamAccessKey, iamSecretKey)
                .regionName(DEFAULT_REGION)
                .serviceName(DEFAULT_SERVICE_NAME)
                .httpMethodName(DEFAULT_METHOD)
                .canonicalURI(DEFAULT_CANONICAL_URI) //end point
                .queryParametes(null)
                .awsHeaders(awsHeaders)
                .payload(DEFAULT_PAYLOAD)
                .build();

        String authorization = awsV4Auth.getHeaders().get("Authorization");

        Map<String, String> headers = new HashMap();
        headers.put("Authorization", authorization);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("X-Amz-Date", awsV4Auth.getxAmzDate());
        getIamServerId().ifPresent(serverId -> headers.put("x-vault-aws-iam-server-id", serverId));
        if (iamServerId != null && !iamServerId.isEmpty()) {
            headers.put("x-vault-aws-iam-server-id", iamServerId);
        }
        Gson gson = new Gson();
        String textOfHeaders = gson.toJson(headers);

        return textOfHeaders;
    }

    public Optional<String> getIamServerId() {
        return Optional.ofNullable(iamServerId).filter(s -> !s.trim().isEmpty());
    }

}
