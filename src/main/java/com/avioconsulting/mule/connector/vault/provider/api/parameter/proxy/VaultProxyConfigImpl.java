package com.avioconsulting.mule.connector.vault.provider.api.parameter.proxy;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.Objects;

@Alias("proxy")
@TypeDsl(allowTopLevelDefinition = true)
public class VaultProxyConfigImpl implements VaultProxyConfig {

    @Parameter
    @Summary("Host to route proxied requests to")
    private String host;

    @Parameter
    @Summary("Port to route proxied requests to")
    private int port;

    @Parameter
    @Optional
    @Summary("Username for proxy authentication")
    private String username;

    @Parameter
    @Optional
    @Password
    @Summary("Password for proxy authentication")
    private String password;

    @Parameter
    @Optional
    @Summary("List of comma separated hosts with which the proxy should not be used")
    private String nonProxyHosts;

    @Override
    public int hashCode() {
        return Objects.hash(host, port, username, password, nonProxyHosts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VaultProxyConfigImpl that = (VaultProxyConfigImpl) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(nonProxyHosts, that.nonProxyHosts);
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }
}
