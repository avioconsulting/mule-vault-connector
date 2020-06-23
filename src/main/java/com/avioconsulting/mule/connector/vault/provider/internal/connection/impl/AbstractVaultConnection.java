package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.VaultResponseAttributes;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.configuration.ConfigurationOverrides;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.api.client.VaultClient;
import com.avioconsulting.mule.vault.api.client.VaultConfig;
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

    public AbstractVaultConnection() {
        this.config = new VaultConfig();
    };

    public AbstractVaultConnection(VaultConfig config) {
        this.config = config;
    }

    @Override
    public void invalidate() {
    }

    @Override
    public boolean isValid() throws VaultAccessException, DefaultMuleException {
        try {
            if (vault.getToken() == null || vault.getToken().isEmpty()) {
                vault.authenticate();
            }
            return vault.validateToken();
        } catch (AccessException e) {
            throw new VaultAccessException(e);
        } catch (VaultException e) {
            throw new DefaultMuleException(e);
        }
    }

    @Override
    public Result<InputStream, VaultResponseAttributes> getSecret(String path, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException {
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
    public Result<InputStream, VaultResponseAttributes> writeSecret(String path, String secret, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException {
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
    public Result<InputStream, VaultResponseAttributes> encryptData(String transitMountpoint, String keyName, String plaintext, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException {
        VaultRequestBuilder builder = new VaultRequestBuilder().
                config(config).
                followRedirects(overrides.isFollowRedirects()).
                responseTimeout(overrides.getResponseTimeout(), overrides.getResponseTimeoutUnit()).
                kvVersion(overrides.getEngineVersion().getEngineVersionNumber()).
                secretPath(transitMountpoint + "/encrypt/" + keyName);

        JsonObject jo = new JsonObject();
        jo.addProperty("plaintext", plaintext);
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
    public Result<InputStream, VaultResponseAttributes> decryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException {
        VaultRequestBuilder builder = new VaultRequestBuilder().
                config(config).
                followRedirects(overrides.isFollowRedirects()).
                responseTimeout(overrides.getResponseTimeout(), overrides.getResponseTimeoutUnit()).
                kvVersion(overrides.getEngineVersion().getEngineVersionNumber()).
                secretPath(transitMountpoint + "/decrypt/" + keyName);
        JsonObject jo = new JsonObject();
        jo.addProperty("ciphertext", ciphertext);
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
    public Result<InputStream, VaultResponseAttributes> reencryptData(String transitMountpoint, String keyName, String ciphertext, ConfigurationOverrides overrides) throws DefaultMuleException, VaultAccessException, SecretNotFoundException {
        VaultRequestBuilder builder = new VaultRequestBuilder().
                config(config).
                followRedirects(overrides.isFollowRedirects()).
                responseTimeout(overrides.getResponseTimeout(), overrides.getResponseTimeoutUnit()).
                kvVersion(overrides.getEngineVersion().getEngineVersionNumber()).
                secretPath(transitMountpoint + "/rewrap/" + keyName);

        JsonObject jo = new JsonObject();
        jo.addProperty("ciphertext", ciphertext);
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
