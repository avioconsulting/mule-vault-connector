package com.avioconsulting.mule.connector.vault.provider.api.parameter;

import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

/**
 * Grouping of properties used for TLS authentication.
 * Only one of the two parameters is needed
 */
@ExclusiveOptionals
public class TLSAuthProperties {

    @DisplayName("JKS Properties")
    @Parameter
    @Optional
    private JKSProperties jksProperties;

    @DisplayName("PEM Properties")
    @Parameter
    @Optional
    private PEMProperties pemProperties;

    public JKSProperties getJksProperties() {
        return jksProperties;
    }

    public void setJksProperties(JKSProperties jksProperties) {
        this.jksProperties = jksProperties;
    }

    public PEMProperties getPemProperties() {
        return pemProperties;
    }

    public void setPemProperties(PEMProperties pemProperties) {
        this.pemProperties = pemProperties;
    }
}
