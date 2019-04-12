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

import java.time.Clock;

public class Ec2VaultConnection extends AbstractVaultConnection {

    private final Logger LOGGER = LoggerFactory.getLogger(Ec2VaultConnection.class);

    public Ec2VaultConnection(String id, String vaultUrl, String role, String pkcs7, String nonce, String identity,
                              String signature, String awsAuthMount, SSLProperties sslProperties,
                              EngineVersion engineVersion) throws ConnectionException {
        this.id = id;
        this.vaultConfig = new VaultConfig().address(vaultUrl);
        if (engineVersion != null) {
            this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
        }
        try {
            SslConfig ssl = getVaultSSLConfig(sslProperties);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            Vault vaultDriver = new Vault(this.vaultConfig.build());
            AuthResponse response = null;
            if (pkcs7 != null) {
                response = vaultDriver.auth().loginByAwsEc2(role, pkcs7, nonce, awsAuthMount);
            } else {
                response= vaultDriver.auth().loginByAwsEc2(role, identity, signature, nonce, awsAuthMount);
            }
            this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
            this.renewable = response.getRenewable();
            this.expirationTime = Clock.systemDefaultZone().instant().plusSeconds(response.getAuthLeaseDuration());
            this.vault = new Vault(this.vaultConfig.build());

        } catch (VaultException ve) {
            LOGGER.error("Error connecting to Vault", ve);
            throw new ConnectionException(ve);
        }
    }

}
