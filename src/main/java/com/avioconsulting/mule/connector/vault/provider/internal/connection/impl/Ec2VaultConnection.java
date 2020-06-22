package com.avioconsulting.mule.connector.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.connector.vault.provider.api.error.exception.VaultAccessException;
import com.avioconsulting.mule.vault.api.client.VaultClient;
import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.VaultConfigBuilder;
import com.avioconsulting.mule.vault.api.client.auth.AWSEC2Authenticator;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A connection to Vault using EC2 properties for authentication
 *
 * @author Adam Mead
 */
public class Ec2VaultConnection extends AbstractVaultConnection {

    private static final Logger logger = LoggerFactory.getLogger(Ec2VaultConnection.class);

    public Ec2VaultConnection(String vaultUrl, String authMount, String awsRole, HttpClient httpClient, String pkcs7, String nonce, String identity, String signature, boolean useInstanceMetadata, Integer responseTimeout, TimeUnit responseTimeoutUnit, boolean followRedirects) throws DefaultMuleException {
        VaultConfigBuilder builder = VaultConfig.builder().
                baseUrl(vaultUrl).
                authenticator(new AWSEC2Authenticator(authMount, awsRole, pkcs7, nonce, identity, signature, useInstanceMetadata)).
                httpClient(httpClient).
                timeout(responseTimeout).
                timeoutUnit(responseTimeoutUnit).
                kvVersion(1).
                followRedirects(followRedirects);

        this.config = builder.build();
        this.vault = new VaultClient(builder.build());
        try {
            this.vault.authenticate();
        } catch (AccessException e) {
            throw new VaultAccessException(e);
        } catch (VaultException e) {
            throw new DefaultMuleException(e);
        }
    }
}
