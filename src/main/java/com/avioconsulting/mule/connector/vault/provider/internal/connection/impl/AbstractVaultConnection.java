package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.VaultResponseAttributes;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.configuration.ConfigurationOverrides;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.api.client.VaultClient;
import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.VaultConstants;
import com.avioconsulting.mule.vault.api.client.VaultRequestBuilder;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import com.google.gson.JsonObject;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Abstract class implementing common methods on a VaultConnection
 *
 * @author Adam Mead
 */
public abstract class AbstractVaultConnection implements VaultConnection {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVaultConnection.class);

    // using local config
    VaultClient vault;
    VaultConfig config;

    boolean validConnection;

    public AbstractVaultConnection(VaultConfig config) {
        this.config = config;
    }

    @Override
    public void invalidate() {
        logger.info("Invalidating connection");
        this.vault.invalidate();
        this.validConnection = false;
    }

    @Override
    public boolean isValid() {
        logger.info(String.format("isValid(): %b", validConnection));
        return validConnection;
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> getSecret(String path, ConfigurationOverrides overrides) throws DefaultMuleException, InterruptedException {
        logger.info(String.format("Getting secret from path (%s)", path));
        VaultRequestBuilder builder = new VaultRequestBuilder().
                config(config).
                followRedirects(overrides.isFollowRedirects()).
                responseTimeout(overrides.getResponseTimeout(), overrides.getResponseTimeoutUnit()).
                kvVersion(overrides.getEngineVersion().getEngineVersionNumber()).
                secretPath(path);

        try {
            return vault.getSecret(builder.build());
        } catch (AccessException e) {
            throw new VaultAccessException(e);
        } catch (com.avioconsulting.mule.vault.api.client.exception.SecretNotFoundException e) {
            throw new SecretNotFoundException(e);
        } catch (VaultException e) {
            throw new DefaultMuleException(e);
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> writeSecret(String path, String secret, ConfigurationOverrides overrides) throws DefaultMuleException, InterruptedException {
        logger.info(String.format("Writing string to path (%s)", path));
        VaultRequestBuilder builder = new VaultRequestBuilder().
                config(config).
                followRedirects(overrides.isFollowRedirects()).
                responseTimeout(overrides.getResponseTimeout(), overrides.getResponseTimeoutUnit()).
                kvVersion(overrides.getEngineVersion().getEngineVersionNumber()).
                secretPath(path).
                payload(secret);
        try {
            return vault.writeSecret(builder.build());
        } catch (AccessException e) {
            throw new VaultAccessException(e);
        } catch (com.avioconsulting.mule.vault.api.client.exception.SecretNotFoundException e) {
            throw new SecretNotFoundException(e);
        } catch (VaultException e) {
            throw new DefaultMuleException(e);
        }

    }

    @Override
    public Result<InputStream, VaultResponseAttributes> encryptData(String transitMountpoint, String keyName, String plaintext, ConfigurationOverrides overrides) throws DefaultMuleException, InterruptedException {
        logger.info(String.format("Encrypting data with mount point (%s) and key (%s)", transitMountpoint, keyName));
        VaultRequestBuilder builder = new VaultRequestBuilder().
                config(config).
                followRedirects(overrides.isFollowRedirects()).
                responseTimeout(overrides.getResponseTimeout(), overrides.getResponseTimeoutUnit()).
                kvVersion(overrides.getEngineVersion().getEngineVersionNumber()).
                secretPath(transitMountpoint + "/encrypt/" + keyName);

        JsonObject jo = new JsonObject();
        jo.addProperty(VaultConstants.PLAINTEXT_ATTRIBUTE, plaintext);
        try {
            return vault.encryptData(builder.payload(jo.toString()).build());
        } catch (AccessException e) {
            throw new VaultAccessException(e);
        } catch (com.avioconsulting.mule.vault.api.client.exception.SecretNotFoundException e) {
            throw new SecretNotFoundException(e);
        } catch (VaultException e) {
            throw new DefaultMuleException(e);
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> decryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws DefaultMuleException, InterruptedException {
        logger.info(String.format("Decrypting data with mount point (%s) and key (%s)", transitMountpoint, keyName));
        VaultRequestBuilder builder = new VaultRequestBuilder().
                config(config).
                followRedirects(overrides.isFollowRedirects()).
                responseTimeout(overrides.getResponseTimeout(), overrides.getResponseTimeoutUnit()).
                kvVersion(overrides.getEngineVersion().getEngineVersionNumber()).
                secretPath(transitMountpoint + "/decrypt/" + keyName);
        JsonObject jo = new JsonObject();
        jo.addProperty(VaultConstants.CIPHERTEXT_ATTRIBUTE, ciphertext);
        try {
            return vault.decryptData(builder.payload(jo.toString()).build());
        } catch (AccessException e) {
            throw new VaultAccessException(e);
        } catch (com.avioconsulting.mule.vault.api.client.exception.SecretNotFoundException e) {
            throw new SecretNotFoundException(e);
        } catch (VaultException e) {
            throw new DefaultMuleException(e);
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> reencryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws DefaultMuleException, InterruptedException {
        logger.info(String.format("Re-encrypting data with mount point (%s) and key (%s)", transitMountpoint, keyName));
        VaultRequestBuilder builder = new VaultRequestBuilder().
                config(config).
                followRedirects(overrides.isFollowRedirects()).
                responseTimeout(overrides.getResponseTimeout(), overrides.getResponseTimeoutUnit()).
                kvVersion(overrides.getEngineVersion().getEngineVersionNumber()).
                secretPath(transitMountpoint + "/rewrap/" + keyName);

        JsonObject jo = new JsonObject();
        jo.addProperty(VaultConstants.CIPHERTEXT_ATTRIBUTE, ciphertext);
        try {
            return vault.reencryptData(builder.payload(jo.toString()).build());
        } catch (AccessException e) {
            throw new VaultAccessException(e);
        } catch (com.avioconsulting.mule.vault.api.client.exception.SecretNotFoundException e) {
            throw new SecretNotFoundException(e);
        } catch (VaultException e) {
            throw new DefaultMuleException(e);
        }
    }
}
