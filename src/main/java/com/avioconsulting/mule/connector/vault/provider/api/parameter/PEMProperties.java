package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * Properties used for TLS authentication via PEM files
 */
public class PEMProperties {

    @DisplayName("Client PEM File")
    @Summary("An X.509 client certificate, for use with Vault's TLS Certificate auth backend")
    @Path
    @Parameter
    private String clientPemFile;

    @DisplayName("Client Key PEM File")
    @Summary("An RSA private key, for use with Vault's TLS Certificate auth backend")
    @Path
    @Parameter
    private String clientKeyPemFile;

    public String getClientPemFile() {
        return clientPemFile;
    }

    public void setClientPemFile(String clientPemFile) {
        this.clientPemFile = clientPemFile;
    }

    public String getClientKeyPemFile() {
        return clientKeyPemFile;
    }

    public void setClientKeyPemFile(String clientKeyPemFile) {
        this.clientKeyPemFile = clientKeyPemFile;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("clientPemFile: ");
        sb.append(clientPemFile);
        sb.append(", clientKeyPemFile: ");
        sb.append(clientKeyPemFile);
        return sb.toString();
    }
}
