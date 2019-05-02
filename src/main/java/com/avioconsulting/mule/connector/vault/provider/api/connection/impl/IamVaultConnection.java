package com.avioconsulting.mule.connector.vault.provider.api.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.SSLProperties;
import com.bettercloud.vault.SslConfig;
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

/**
 * A connection to Vault using IAM to authenticate
 *
 * @author Adam Mead
 */
public class IamVaultConnection extends AbstractVaultConnection {

    private final static String UTF_8 = "UTF-8";

    private final Logger LOGGER = LoggerFactory.getLogger(IamVaultConnection.class);

    /**
     * Construct a connection using IAM to authenticate
     *
     * @param id                   ID for the connection
     * @param vaultUrl             URL for the Vault server (https://host:port)
     * @param awsAuthMount         AWS auth mount
     * @param role                 Name of the role against which the login is being attempted. If role is not specified,
     *                             then the login endpoint looks for a role bearing the name of the AMI ID of the EC2
     *                             instance that is trying to login if using the ec2 auth method, or the "friendly name"
     *                             (i.e., role name or username) of the IAM principal authenticated. If a matching role
     *                             is not found, login fails
     * @param iamRequestUrl        HTTP URL used in the signed request. Most likely https://sts.amazonaws.com/ as most
     *                             requests will probably use POST with an empty URI
     * @param iamRequestBody       Body of the signed request. Most likely Action=GetCallerIdentity&Version=2011-06-15
     * @param iamRequestHeaders    Request headers
     * @param sslProperties        {@link SSLProperties} to use to make the connection
     * @param engineVersion        The version of the secret engine to use, defaulting to Version 2
     * @throws ConnectionException if there is an issue connecting to Vault
     */
    public IamVaultConnection(String id, String vaultUrl, String awsAuthMount, String role, String iamRequestUrl,
                              String iamRequestBody, String iamRequestHeaders, SSLProperties sslProperties,
                              EngineVersion engineVersion) throws ConnectionException {
        this.id = id;
        try {
            // iamRequestUrl and iamRequestBody need to be base64 encoded
            String requestUrl_b64 = Base64.getEncoder().encodeToString(iamRequestUrl.getBytes(UTF_8));
            String requestBody_b64 = Base64.getEncoder().encodeToString(iamRequestBody.getBytes(UTF_8));
            this.vaultConfig = new VaultConfig().address(vaultUrl);
            if (engineVersion != null) {
                this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
            }
            SslConfig ssl = getVaultSSLConfig(sslProperties);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            Vault vaultDriver = new Vault(this.vaultConfig.build());
            AuthResponse response = vaultDriver.auth().loginByAwsIam(role, requestUrl_b64, requestBody_b64, iamRequestHeaders, awsAuthMount);
            this.renewable = response.getRenewable();
            this.expirationTime = Clock.systemDefaultZone().instant().plusSeconds(response.getAuthLeaseDuration());
            this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
            this.vault = new Vault(this.vaultConfig.build());
            this.valid = true;
        } catch (VaultException ve) {
            LOGGER.error("Error connecting to Vault", ve);
            throw new ConnectionException(ve);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error connecting to Vault", e);
            throw new ConnectionException(e);
        }
    }
}
