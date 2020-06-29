package com.avioconsulting.mule.vault.api.client.auth;

import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TLSAuthenticator extends AbstractAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(TLSAuthenticator.class);

    private static final String DEFAULT_AUTH_MOUNT = "cert";

    private String authMount;
    private String certificateRole;

    public TLSAuthenticator(String authMount, String certificateRole) {
        this.authMount = authMount;
        this.certificateRole = certificateRole;
    }

    @Override
    public String getAuthPath() {
        String mount = DEFAULT_AUTH_MOUNT;

        if (authMount != null && !authMount.isEmpty()) {
            mount = authMount;
        }
        logger.debug("Authentication Mount; {}", mount);
        return String.format("auth/%s/login", mount);
    }

    @Override
    public String getAuthPayload(VaultConfig config) throws VaultException {
        JsonObject json = new JsonObject();
        if (this.certificateRole != null && !this.certificateRole.isEmpty()) {
            json.addProperty("name", this.certificateRole);
        }
        logger.debug("Authentication payload: {}", json);
        return json.toString();
    }
}
