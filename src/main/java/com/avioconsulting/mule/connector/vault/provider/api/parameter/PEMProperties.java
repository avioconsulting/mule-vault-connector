package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class PEMProperties {

    @DisplayName("Vault PEM File")
    @Summary("An X.509 certificate, to use when communicating with Vault over HTTPS")
    @Path
    @Parameter
    private String pemFile;

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

    public String getPemFile() {
        return pemFile;
    }

    public void setPemFile(String pemFile) {
        this.pemFile = pemFile;
    }

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
        sb.append("pemFile: ");
        sb.append(pemFile);
        sb.append(", clientPemFile: ");
        sb.append(clientPemFile);
        sb.append(", clientKeyPemFile: ");
        sb.append(clientKeyPemFile);
        return sb.toString();
    }
}
