package com.avioconsulting.mule.vault.api.client.auth;

import com.avioconsulting.mule.vault.api.client.VaultConfig;
import com.avioconsulting.mule.vault.api.client.exception.AccessException;
import com.avioconsulting.mule.vault.api.client.exception.VaultException;

public class TokenAuthenticator implements VaultAuthenticator {

    private String token;

    public TokenAuthenticator(String token) {
        this.token = token;
    }

    @Override
    public String authenticate(VaultConfig config) throws AccessException, VaultException {
        if (token != null) {
            return token;
        } else{
            throw new VaultException("No token provided");
        }
    }
}
