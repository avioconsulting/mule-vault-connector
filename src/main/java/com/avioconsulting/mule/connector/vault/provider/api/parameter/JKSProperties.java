package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * Properties used for TLS authentication via Java KeyStores
 */
public class JKSProperties {

    @DisplayName("KeyStore File")
    @Summary("Path to the KeyStore if using Vault's TLS Certificate auth backend for client side authentication. The KeyStore password must also be provided.")
    @Path
    @Parameter
    private String keyStoreFile;

    @DisplayName("KeyStore Password")
    @Password
    @Parameter
    private String keyStorePassword;

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("keyStoreFile: ");
        sb.append(keyStoreFile);
        sb.append(", keyStorePassword: ");
        sb.append(keyStorePassword);
        return  sb.toString();
    }
}
