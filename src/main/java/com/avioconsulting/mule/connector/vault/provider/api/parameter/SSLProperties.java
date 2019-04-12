package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.io.File;
import java.net.URL;

public class SSLProperties {

    @DisplayName("Vault PEM File")
    @Summary("An X.509 certificate, to use when communicating with Vault over HTTPS")
    @Parameter
    @Optional
    @Path
    private String pemFile;

    @DisplayName("TrustStore File")
    @Parameter
    @Optional
    @Path
    private String trustStoreFile;

    public String getPemFile() {
        return pemFile;
    }

    public void setPemFile(String pemFile) {
        this.pemFile = pemFile;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

}
