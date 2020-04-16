package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.UnknownVaultException;
import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.connector.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.bettercloud.vault.response.LogicalResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class implementing common methods on a VaultConnection
 *
 * @author Adam Mead
 */
public abstract class AbstractVaultConnection implements VaultConnection {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVaultConnection.class);

    String id;
    boolean valid = false;
    Vault vault;
    VaultConfig vaultConfig;
    boolean renewable;
    Instant expirationTime;

    public AbstractVaultConnection() {
        id = null;
        vault = null;
        vaultConfig = new VaultConfig();
        renewable = false;
        expirationTime = Clock.systemDefaultZone().instant();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Vault getVault() {
        return vault;
    }

    @Override
    public void invalidate() {
        this.valid = false;
        this.vault = null;
    }

    @Override
    public boolean isValid() {
        if (expirationTime != null) {
            if (expirationTime.isBefore(Clock.systemDefaultZone().instant())) {
                renewLease();
            } else {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Renew the Vault Token to keep it valid
     */
    @Override
    public void renewLease() {
        if (renewable && expirationTime != null && expirationTime.isBefore(Clock.systemDefaultZone().instant())) {
            try {
                AuthResponse response = vault.auth().renewSelf();
                this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
                this.vault = new Vault(this.vaultConfig.build());
                this.renewable = response.getRenewable();
                this.expirationTime = Clock.systemDefaultZone().instant().plusSeconds(response.getAuthLeaseDuration());
            } catch (VaultException ve) {
                logger.error("Error renewing Vault token",ve);
            }
        }
    }

    /**
     * Construct {@link SslConfig} given the ssl-properties element for HTTPS connections to Vault
     *
     * @param sslProperties properties in the ssl-properties element
     * @return {@link SslConfig} constructed from the ssl-properties attributes
     * @throws VaultException if there is an error constructing the {@link SslConfig} object
     */
    public SslConfig getVaultSSLConfig(SSLProperties sslProperties) throws VaultException {
        SslConfig ssl = new SslConfig();
        if (sslProperties != null) {
            if (sslProperties.getPemFile() != null && !sslProperties.getPemFile().isEmpty()) {
                if (classpathResourceExists(sslProperties.getPemFile())) {
                    ssl = ssl.pemResource(sslProperties.getPemFile());
                } else {
                    ssl = ssl.pemFile(new File(sslProperties.getPemFile()));
                }
                ssl = ssl.verify(true);
            } else if (sslProperties.getTrustStoreFile() != null && !sslProperties.getTrustStoreFile().isEmpty()) {
                if (classpathResourceExists(sslProperties.getTrustStoreFile())) {
                    ssl = ssl.trustStoreResource(sslProperties.getTrustStoreFile());
                } else {
                    ssl = ssl.trustStoreFile(new File(sslProperties.getTrustStoreFile()));
                }
                ssl = ssl.verify(true);
            }
        }
        return ssl;
    }

    /**
     * Determine if the path resides on the classpath
     *
     * @param path the path to the file
     * @return true if the file is on the classpath
     */
    protected boolean classpathResourceExists(String path) {
        boolean fileExists = false;
        URL fileUrl = getClass().getResource(path);
        if (fileUrl != null) {
            File file = new File(fileUrl.getFile());
            if (file != null) {
                fileExists = file.exists();
            }
        }
        return fileExists;
    }

    @Override
    public String getSecret(String path) throws VaultAccessException, SecretNotFoundException, UnknownVaultException {
        try {
            Gson gson = new GsonBuilder().create();
            return gson.toJson(getVault().logical().read(path).getData());
        } catch (VaultException ve) {
            if (ve.getHttpStatusCode() == 404) {
                logger.error("Secret not found in Vault", ve);
                throw new SecretNotFoundException(ve);
            } else if (ve.getHttpStatusCode() == 403) {
                logger.error("Access denied in Vault", ve);
                throw new VaultAccessException(ve);
            } else {
                logger.error("Unknown Vault Exception", ve);
                throw new UnknownVaultException(ve);
            }
        }
    }

    @Override
    public void writeSecret(String path, String secret) throws VaultAccessException, UnknownVaultException {
        try {
            Gson gson = new Gson();
            Type secretType = new TypeToken<Map<String,Object>>(){}.getType();
            Map<String,Object> secretData = gson.fromJson(secret, secretType);
            getVault().logical().write(path, secretData);
        } catch (VaultException ve) {
            if (ve.getHttpStatusCode() == 403) {
                logger.error("Access denied in Vault", ve);
                throw new VaultAccessException(ve);
            } else {
                logger.error("Unknown Vault Exception", ve);
                throw new UnknownVaultException(ve);
            }
        }
    }

    @Override
    public String encryptData(String transitMountpoint, String keyName, String plaintext) throws VaultAccessException, UnknownVaultException {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("plaintext", Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8)));
            LogicalResponse response = getVault().logical().write(transitMountpoint + "/encrypt/" + keyName, data);
            return response.getData().get("ciphertext");
        } catch (VaultException ve) {
            if (ve.getHttpStatusCode() == 403) {
                logger.error("Access denied in Vault", ve);
                throw new VaultAccessException(ve);
            } else {
                logger.error("Unknown Vault Exception", ve);
                throw new UnknownVaultException(ve);
            }
        }
    }

    @Override
    public String decryptData(String transitMountpoint, String keyName, String ciphertext) throws VaultAccessException, UnknownVaultException {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("ciphertext", ciphertext);
            LogicalResponse response = getVault().logical().write(transitMountpoint + "/decrypt/" + keyName, data);
            String decrypted = new String(Base64.getDecoder().decode(response.getData().get("plaintext")), StandardCharsets.UTF_8);
            return decrypted;
        } catch (VaultException ve) {
            if (ve.getHttpStatusCode() == 403) {
                logger.error("Access denied in Vault", ve);
                throw new VaultAccessException(ve);
            } else {
                logger.error("Unknown Vault Exception", ve);
                throw new UnknownVaultException(ve);
            }
        }
    }

    @Override
    public String reencryptData(String transitMountpoint, String keyName, String ciphertext) throws VaultAccessException, UnknownVaultException {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("ciphertext", ciphertext);
            LogicalResponse response = getVault().logical().write(transitMountpoint + "/rewrap/" + keyName, data);
            return response.getData().get("ciphertext");
        } catch (VaultException ve) {
            if (ve.getHttpStatusCode() == 403) {
                logger.error("Access denied in Vault", ve);
                throw new VaultAccessException(ve);
            } else {
                logger.error("Unknown Vault Exception", ve);
                throw new UnknownVaultException(ve);
            }
        }
    }
}
