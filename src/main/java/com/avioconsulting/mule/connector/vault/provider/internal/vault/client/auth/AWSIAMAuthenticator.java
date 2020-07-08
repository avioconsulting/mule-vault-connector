package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.VaultConfig;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.VaultException;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AWSIAMAuthenticator extends AbstractAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(AWSIAMAuthenticator.class);

    private static final String DEFAULT_AUTH_MOUNT = "aws";

    private String authMount;
    private String role;
    private String iamRequestUrl;
    private String iamRequestBody;
    private String iamRequestHeaders;

    public AWSIAMAuthenticator(String authMount, String role, String iamRequestUrl, String iamRequestBody, String iamRequestHeaders) {
        super();
        this.authMount = authMount;
        this.role = role;
        this.iamRequestUrl = iamRequestUrl;
        this.iamRequestBody = iamRequestBody;
        this.iamRequestHeaders = iamRequestHeaders;
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
        payload.addProperty("iam_request_url", this.iamRequestUrl);
        payload.addProperty("iam_request_headers", this.iamRequestHeaders);
        payload.addProperty("iam_request_body", this.iamRequestBody);
        return payload.toString();
    }
}
