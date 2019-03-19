package com.avioconsulting.mule.connector.vault.internal.connection.impl;

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

    public Ec2VaultConnection(String id, String vaultUrl, String role, String pkcs7, String nonce, String identity, String signature, String awsAuthMount) throws ConnectionException {
        this.id = id;
        vaultConfig = new VaultConfig().address(vaultUrl);
        try {
            Vault vaultDriver = new Vault(vaultConfig.build());
            AuthResponse response = null;
            if (pkcs7 != null) {
                response = vaultDriver.auth().loginByAwsEc2(role, pkcs7, nonce, awsAuthMount);
            } else {
                response= vaultDriver.auth().loginByAwsEc2(role, identity, signature, nonce, awsAuthMount);
            }
            vaultConfig = vaultConfig.token(response.getAuthClientToken());
            renewable = response.getRenewable();
            expirationTime = Clock.systemDefaultZone().instant().plusSeconds(response.getAuthLeaseDuration());
            vault = new Vault(vaultConfig.build());

        } catch (VaultException ve) {
            LOGGER.error("Error connecting to Vault", ve);
            throw new ConnectionException(ve);
        }
    }

}
