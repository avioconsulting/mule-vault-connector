package com.avioconsulting.mule.connector.vault.provider.api.connection.impl;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Clock;
import java.util.Base64;

public class IamVaultConnection extends AbstractVaultConnection {

    private final static String UTF_8 = "UTF-8";

    private final Logger LOGGER = LoggerFactory.getLogger(IamVaultConnection.class);

    public IamVaultConnection(String id, String vaultUrl, String awsAuthMount, String role, String iamRequestUrl, String iamRequestBody, String iamRequestHeaders) throws ConnectionException {
        this.id = id;
        try {
            // iamRequestUrl and iamRequestBody need to be base64 encoded
            String requestUrl_b64 = Base64.getEncoder().encodeToString(iamRequestUrl.getBytes(UTF_8));
            String requestBody_b64 = Base64.getEncoder().encodeToString(iamRequestBody.getBytes(UTF_8));
            VaultConfig config = new VaultConfig().address(vaultUrl);
            Vault vaultDriver = new Vault(config.build());
            AuthResponse response = vaultDriver.auth().loginByAwsIam(role, requestUrl_b64, requestBody_b64, iamRequestHeaders, awsAuthMount);
            renewable = response.getRenewable();
            expirationTime = Clock.systemDefaultZone().instant().plusSeconds(response.getAuthLeaseDuration());
            this.vaultConfig = config.token(response.getAuthClientToken());
            vault = new Vault(vaultConfig.build());
        } catch (VaultException ve) {
            LOGGER.error("Error connecting to Vault", ve);
            throw new ConnectionException(ve);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error connecting to Vault", e);
            throw new ConnectionException(e);
        }
    }
}
