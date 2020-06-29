package com.avioconsulting.mule.vault.api.client.auth;

import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.VaultConstants;
import com.avioconsulting.mule.vault.api.client.VaultRequestBuilder;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TokenAuthenticator extends AbstractAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticator.class);
    private static final String SKIP_LOOKUP_PROPERTY="VAULT_SKIP_TOKEN_LOOKUP";

    private String token;

    public TokenAuthenticator(String token) {
        this.token = token;
    }

    @Override
    public String authenticate(VaultConfig config) throws VaultException, InterruptedException {
        String skipLookup = System.getProperty(SKIP_LOOKUP_PROPERTY);
        if ("TRUE".equalsIgnoreCase(skipLookup)) {
            return token;
        }

        try {
            HttpRequestBuilder builder = new VaultRequestBuilder().
                    config(config).
                    kvVersion(1).
                    secretPath("auth/token/lookup").
                    build().
                    getHttpRequestBuilder().
                    method(HttpConstants.Method.GET).
                    addHeader(VaultConstants.VAULT_TOKEN_HEADER, token);

            CompletableFuture<HttpResponse> completable = config.getHttpClient().sendAsync(builder.build(), config.getTimeoutInMilliseconds(), config.isFollowRedirects(), null);

            HttpResponse response = completable.get();

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                return token;
            } else {
                throw new VaultException("No token provided");
            }
        } catch (ExecutionException e) {
            logger.error("Exception encountered while doing token lookup", e);
            throw new VaultException(e);
        }
    }

}
