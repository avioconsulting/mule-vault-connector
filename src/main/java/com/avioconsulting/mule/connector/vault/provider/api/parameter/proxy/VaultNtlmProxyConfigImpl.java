package com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy;

import static com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy.VaultProxyConfig.VaultNtlmProxyConfig;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import java.util.Objects;

@Alias("ntlm-proxy")
@TypeDsl(allowTopLevelDefinition = true)
public class VaultNtlmProxyConfigImpl extends VaultProxyConfigImpl implements VaultNtlmProxyConfig {
    @Parameter
    @DisplayName("NTLM Domain")
    private String ntlmDomain;

    public String getNtlmDomain() {
        return ntlmDomain;
    }

    public void setNtlmDomain(String ntlmDomain) {
        this.ntlmDomain = ntlmDomain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        VaultNtlmProxyConfig that = (VaultNtlmProxyConfig) o;
        return Objects.equals(ntlmDomain, that.getNtlmDomain());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ntlmDomain);
    }
}
