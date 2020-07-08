package com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy;

import org.mule.runtime.http.api.client.proxy.ProxyConfig;

public interface VaultProxyConfig extends ProxyConfig {
    interface VaultNtlmProxyConfig extends ProxyConfig.NtlmProxyConfig {
    }
}
